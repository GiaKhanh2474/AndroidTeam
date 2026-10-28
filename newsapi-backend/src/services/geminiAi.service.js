/**
 * geminiAi.service.js
 * Gửi bài báo thô qua Gemini (model gemini-2.5-flash) để:
 *   - phân loại chuyên mục (KINH_TE | CHINH_TRI | CONG_NGHE | BEN_LE)
 *   - viết main_grid_summary (15–20 từ) và card_summary (35–50 từ)
 *   - chấm ai_score (1–100) và đặt is_main_featured (ai_score >= 80)
 *
 * Cơ chế fallback an toàn: nếu chưa có GEMINI_API_KEY, hết quota (429) hoặc Gemini trả dữ liệu
 * không dùng được thì tự cắt chuỗi (18 từ đầu / 45 từ đầu) và suy luận category theo URL nguồn RSS,
 * nên Server không bao giờ bị crash.
 */
const { Type } = require("@google/genai");
const { getGeminiClient, getModelName } = require("../config/gemini");

const CATEGORIES = ["KINH_TE", "CHINH_TRI", "CONG_NGHE", "BEN_LE"];

const MAIN_SUMMARY_MAX_WORDS = 20;
const CARD_SUMMARY_MAX_WORDS = 50;
const FALLBACK_MAIN_WORDS = 18;
const FALLBACK_CARD_WORDS = 45;
const FEATURED_THRESHOLD = 80;
const MAX_CONTENT_CHARS = 6000; // cắt bớt nội dung gửi sang Gemini để tiết kiệm token
const QUOTA_COOLDOWN_MS = 10 * 60 * 1000; // sau khi dính 429, dùng fallback trong 10 phút

const SYSTEM_INSTRUCTION = `Bạn là biên tập viên AI của ứng dụng đọc báo AndroidTeamNews.
Hãy phân tích tiêu đề và nội dung bài báo tiếng Việt dưới đây và trả về kết quả DUY NHẤT dưới dạng JSON hợp lệ (không có markdown \`\`\`json) với cấu trúc:
{
  "category": "Chọn đúng 1 trong 4 nhãn: KINH_TE, CHINH_TRI, CONG_NGHE, BEN_LE",
  "main_grid_summary": "Câu tóm tắt siêu cô đọng từ 15 đến 20 từ tiếng Việt để vừa khít ô vuông 3x3 Grid trên màn hình chính",
  "card_summary": "Đoạn tóm tắt rõ ý từ 35 đến 50 từ tiếng Việt để hiển thị làm dòng chữ nhỏ bên dưới tiêu đề trên thẻ nằm ngang",
  "ai_score": "Số nguyên từ 1 đến 100 đánh giá mức độ quan trọng và ảnh hưởng của tin tức này",
  "is_main_featured": "true nếu ai_score >= 80, ngược lại là false"
}`;

// Schema ép kiểu đầu ra của Gemini (ai_score là số nguyên, is_main_featured là boolean thật)
const RESPONSE_SCHEMA = {
  type: Type.OBJECT,
  properties: {
    category: { type: Type.STRING, enum: CATEGORIES },
    main_grid_summary: { type: Type.STRING },
    card_summary: { type: Type.STRING },
    ai_score: { type: Type.INTEGER },
    is_main_featured: { type: Type.BOOLEAN },
  },
  required: ["category", "main_grid_summary", "card_summary", "ai_score", "is_main_featured"],
};

const state = { blockedUntil: 0 };

// ---------------------------------------------------------------------------
// Tiện ích văn bản
// ---------------------------------------------------------------------------
const ENTITIES = { "&amp;": "&", "&lt;": "<", "&gt;": ">", "&quot;": '"', "&#39;": "'", "&nbsp;": " " };

function stripHtml(text) {
  if (typeof text !== "string") return "";
  return text
    .replace(/<[^>]*>/g, " ")
    .replace(/&(amp|lt|gt|quot|nbsp|#39);/g, (m) => ENTITIES[m] || " ")
    .replace(/\s+/g, " ")
    .trim();
}

function splitWords(text) {
  return typeof text === "string" ? text.trim().split(/\s+/).filter(Boolean) : [];
}

function countWords(text) {
  return splitWords(text).length;
}

function truncateWords(text, maxWords) {
  const words = splitWords(text);
  if (words.length <= maxWords) return words.join(" ");
  return words.slice(0, maxWords).join(" ").replace(/[,;:\-–—\s]+$/, "") + "…";
}

// ---------------------------------------------------------------------------
// Fallback (không dùng AI)
// ---------------------------------------------------------------------------
function inferCategoryFromUrl(url) {
  const u = String(url || "").toLowerCase();
  if (/kinh-doanh|kinh-te|tai-chinh|chung-khoan|bat-dong-san/.test(u)) return "KINH_TE";
  if (/thoi-su|the-gioi|chinh-tri|phap-luat/.test(u)) return "CHINH_TRI";
  if (/so-hoa|cong-nghe|nhip-song-so|khoa-hoc-cong-nghe/.test(u)) return "CONG_NGHE";
  return "BEN_LE";
}

/**
 * @param {{title?: string, content?: string, category_hint?: string, feed_url?: string}} input
 */
function fallbackAnalyze({ title, content, category_hint, feed_url }) {
  const text = stripHtml(content) || stripHtml(title) || "";
  const category = CATEGORIES.includes(category_hint) ? category_hint : inferCategoryFromUrl(feed_url);
  return {
    category,
    main_grid_summary: truncateWords(text, FALLBACK_MAIN_WORDS) || stripHtml(title) || "Bài báo mới",
    card_summary: truncateWords(text, FALLBACK_CARD_WORDS) || stripHtml(title) || "Bài báo mới",
    ai_score: 50,
    is_main_featured: false,
    ai_used: false,
  };
}

// ---------------------------------------------------------------------------
// Chuẩn hóa kết quả Gemini
// ---------------------------------------------------------------------------
function parseJsonLoose(text) {
  if (typeof text !== "string") return null;
  const cleaned = text.replace(/```json|```/gi, "").trim();
  try {
    return JSON.parse(cleaned);
  } catch (_) {
    const start = cleaned.indexOf("{");
    const end = cleaned.lastIndexOf("}");
    if (start >= 0 && end > start) {
      try {
        return JSON.parse(cleaned.slice(start, end + 1));
      } catch (_e) {
        return null;
      }
    }
    return null;
  }
}

/**
 * Kiểm tra / sửa dữ liệu Gemini trả về. Trả về null nếu không dùng được (→ fallback).
 */
function normalizeAiResult(raw, ctx) {
  if (!raw || typeof raw !== "object") return null;

  const main = stripHtml(String(raw.main_grid_summary || ""));
  const card = stripHtml(String(raw.card_summary || ""));
  if (countWords(main) < 5 || countWords(card) < 10) return null;

  const fallbackCategory = CATEGORIES.includes(ctx.category_hint)
    ? ctx.category_hint
    : inferCategoryFromUrl(ctx.feed_url);
  const category = CATEGORIES.includes(String(raw.category || "").trim().toUpperCase())
    ? String(raw.category).trim().toUpperCase()
    : fallbackCategory;

  let score = Math.round(Number(raw.ai_score));
  if (!Number.isFinite(score)) score = 50;
  score = Math.min(100, Math.max(1, score));

  return {
    category,
    main_grid_summary: truncateWords(main, MAIN_SUMMARY_MAX_WORDS),
    card_summary: truncateWords(card, CARD_SUMMARY_MAX_WORDS),
    ai_score: score,
    is_main_featured: score >= FEATURED_THRESHOLD, // luôn tính lại theo quy tắc, không tin giá trị model trả
    ai_used: true,
  };
}

// ---------------------------------------------------------------------------
// Gọi Gemini
// ---------------------------------------------------------------------------
function isQuotaError(err) {
  const msg = String((err && err.message) || "");
  return (err && err.status === 429) || /\b429\b|RESOURCE_EXHAUSTED|quota|rate limit/i.test(msg);
}

function isTransientError(err) {
  return err && (err.status === 500 || err.status === 503 || /UNAVAILABLE|overloaded/i.test(String(err.message || "")));
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function generate(client, prompt, useSchema) {
  const config = {
    systemInstruction: SYSTEM_INSTRUCTION,
    responseMimeType: "application/json",
    
  };
  if (useSchema) config.responseSchema = RESPONSE_SCHEMA;

  const response = await client.models.generateContent({
    model: getModelName(),
    contents: prompt,
    config,
  });
  return response.text;
}

async function callGemini(client, prompt) {
  let useSchema = true;
  let transientRetries = 0;

  for (let attempt = 0; attempt < 4; attempt++) {
    try {
      return await generate(client, prompt, useSchema);
    } catch (err) {
      if (isQuotaError(err)) throw err;
      if (isTransientError(err) && transientRetries < 2) {
        transientRetries += 1;
        await sleep(2000 * transientRetries);
        continue;
      }
      // Lỗi 400 thường do schema bị từ chối → thử lại 1 lần chỉ với responseMimeType JSON
      if (useSchema && err && err.status === 400) {
        useSchema = false;
        continue;
      }
      throw err;
    }
  }
  throw new Error("Gemini không phản hồi sau nhiều lần thử");
}

/**
 * Phân tích 1 bài báo bằng Gemini; luôn trả về kết quả hợp lệ (dùng fallback nếu cần).
 *
 * @param {{title: string, content: string, source_name?: string, category_hint?: string, feed_url?: string}} input
 * @returns {Promise<{category: string, main_grid_summary: string, card_summary: string,
 *                    ai_score: number, is_main_featured: boolean, ai_used: boolean}>}
 */
async function analyzeArticle(input) {
  const ctx = {
    title: input.title,
    content: input.content,
    category_hint: input.category_hint,
    feed_url: input.feed_url,
  };

  const client = getGeminiClient();
  if (!client || Date.now() < state.blockedUntil) return fallbackAnalyze(ctx);

  const body = stripHtml(input.content).slice(0, MAX_CONTENT_CHARS);
  const prompt = `Nguồn báo: ${input.source_name || "Không rõ"}\nTiêu đề: ${stripHtml(input.title)}\n\nNội dung:\n${body}`;

  try {
    const text = await callGemini(client, prompt);
    const normalized = normalizeAiResult(parseJsonLoose(text), ctx);
    if (normalized) return normalized;
    console.warn("[Gemini] Kết quả không hợp lệ, dùng fallback cho bài:", input.title);
    return fallbackAnalyze(ctx);
  } catch (err) {
    if (isQuotaError(err)) {
      state.blockedUntil = Date.now() + QUOTA_COOLDOWN_MS;
      console.warn("[Gemini] Hết quota (429) — dùng fallback trong 10 phút.");
    } else {
      console.warn("[Gemini] Lỗi:", err && err.message ? err.message : err);
    }
    return fallbackAnalyze(ctx);
  }
}

module.exports = {
  analyzeArticle,
  fallbackAnalyze,
  inferCategoryFromUrl,
  // xuất thêm để dễ kiểm thử
  _internal: { normalizeAiResult, parseJsonLoose, truncateWords, countWords, stripHtml, state },
};
