/**
 * rssCrawler.service.js
 * Quét RSS báo Việt Nam → bỏ bài đã có (theo original_url) → lấy toàn văn + ảnh
 * → Gemini phân loại/tóm tắt/chấm điểm → lưu MongoDB → ghi crawl_logs.
 */
const crypto = require("crypto");
const axios = require("axios");
const cheerio = require("cheerio");
const Parser = require("rss-parser");

const Article = require("../models/Article.model");
const CrawlLog = require("../models/CrawlLog.model");
const { analyzeArticle } = require("./geminiAi.service");
const { isGeminiConfigured } = require("../config/gemini");
const { buildDateLabel } = require("../utils/vnDate");

const USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36 AndroidTeamNewsBot/1.0";

/**
 * Danh sách nguồn RSS (Phần 4.1 của đặc tả).
 * category_hint chỉ dùng khi Gemini không trả được chuyên mục hợp lệ (hoặc chạy fallback).
 */
const FEEDS = [
  // 1. Kinh doanh / Kinh tế
  { source_name: "VnExpress", url: "https://vnexpress.net/rss/kinh-doanh.rss", category_hint: "KINH_TE" },
  { source_name: "Tuổi Trẻ", url: "https://tuoitre.vn/rss/kinh-doanh.rss", category_hint: "KINH_TE" },
  // 2. Thời sự / Chính trị - Thế giới
  { source_name: "VnExpress", url: "https://vnexpress.net/rss/thoi-su.rss", category_hint: "CHINH_TRI" },
  { source_name: "VnExpress", url: "https://vnexpress.net/rss/the-gioi.rss", category_hint: "CHINH_TRI" },
  // 3. Công nghệ / Số hóa
  { source_name: "VnExpress", url: "https://vnexpress.net/rss/so-hoa.rss", category_hint: "CONG_NGHE" },
  { source_name: "Tuổi Trẻ", url: "https://tuoitre.vn/rss/nhip-song-so.rss", category_hint: "CONG_NGHE" },
  // 4. Tin bên lề / Đời sống - Khoa học
  { source_name: "VnExpress", url: "https://vnexpress.net/rss/khoa-hoc.rss", category_hint: "BEN_LE" },
  // Đặc tả ghi "ul-doi-song.rss" — nhiều khả năng là gõ nhầm, VnExpress dùng "doi-song.rss"
  { source_name: "VnExpress", url: "https://vnexpress.net/rss/doi-song.rss", category_hint: "BEN_LE" },
];

// Selector bóc nội dung — thử lần lượt, dùng selector đầu tiên cho đủ độ dài
const CONTENT_SELECTORS = [
  "article.fck_detail p.Normal", // VnExpress
  "article.fck_detail p",
  ".detail-content p", // Tuổi Trẻ
  ".detail__content p",
  "#content-detail p",
  ".content-detail p",
  "article p",
];
const MIN_CONTENT_CHARS = 300;

const parser = new Parser({
  timeout: 20000,
  headers: { "User-Agent": USER_AGENT, Accept: "application/rss+xml, application/xml, text/xml, */*" },
  customFields: { item: [["description", "rawDescription"], ["media:content", "mediaContent"]] },
});

let crawling = false;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function envInt(name, fallback) {
  const n = parseInt(process.env[name], 10);
  return Number.isFinite(n) && n >= 0 ? n : fallback;
}

// ---------------------------------------------------------------------------
// Tiện ích
// ---------------------------------------------------------------------------
function normalizeUrl(link) {
  try {
    const u = new URL(String(link).trim());
    u.hash = "";
    for (const key of [...u.searchParams.keys()]) {
      if (/^utm_|^fbclid$|^gclid$/i.test(key)) u.searchParams.delete(key);
    }
    return u.toString();
  } catch (_) {
    return null;
  }
}

function makeArticleId(url) {
  return "art_" + crypto.createHash("sha1").update(url).digest("hex").slice(0, 16);
}

function absoluteUrl(src, base) {
  if (!src) return null;
  try {
    return new URL(src, base).toString();
  } catch (_) {
    return null;
  }
}

function extractImageFromRssItem(item) {
  if (item.enclosure && item.enclosure.url && /^https?:/i.test(item.enclosure.url)) return item.enclosure.url;

  const media = item.mediaContent;
  const mediaUrl = media && (media.$ ? media.$.url : media.url);
  if (mediaUrl) return mediaUrl;

  const html = [item.rawDescription, item.content, item["content:encoded"]].filter(Boolean).join(" ");
  const match = html.match(/<img[^>]+src=["']([^"']+)["']/i);
  return match ? absoluteUrl(match[1], item.link) : null;
}

/**
 * Bóc toàn văn bài báo từ HTML.
 * @returns {{content: string, image: string|null}}
 */
function extractArticleContent(html, pageUrl) {
  const $ = cheerio.load(html);
  $("script, style, noscript, figure, figcaption, .related-news, .box-tinlienquan").remove();

  let content = "";
  for (const selector of CONTENT_SELECTORS) {
    const paragraphs = $(selector)
      .map((_, el) => $(el).text().replace(/\s+/g, " ").trim())
      .get()
      .filter((p) => p.length > 0);
    const joined = paragraphs.join("\n\n");
    if (joined.length >= MIN_CONTENT_CHARS) {
      content = joined;
      break;
    }
  }

  const lead =
    $("p.description").first().text().trim() ||
    $('meta[property="og:description"]').attr("content") ||
    "";
  if (content && lead && !content.startsWith(lead.slice(0, 40))) content = `${lead}\n\n${content}`;

  const image = absoluteUrl($('meta[property="og:image"]').attr("content"), pageUrl);
  return { content, image };
}

async function fetchArticlePage(url) {
  const res = await axios.get(url, {
    timeout: 20000,
    headers: { "User-Agent": USER_AGENT, "Accept-Language": "vi,en;q=0.8" },
    maxContentLength: 5 * 1024 * 1024,
    responseType: "text",
  });
  return res.data;
}

// ---------------------------------------------------------------------------
// Xử lý 1 bài trong RSS
// ---------------------------------------------------------------------------
/**
 * @returns {Promise<"saved"|"saved_ai"|"duplicate"|"skipped">}
 */
async function processFeedItem(item, feed) {
  const originalUrl = normalizeUrl(item.link);
  const title = (item.title || "").replace(/\s+/g, " ").trim();
  if (!originalUrl || !title) return "skipped";

  // 1. Chống trùng theo original_url
  if (await Article.exists({ original_url: originalUrl })) return "duplicate";

  // 2. Ảnh từ RSS (enclosure / <img>), toàn văn từ trang gốc
  let image = extractImageFromRssItem(item);
  let content = "";
  try {
    const html = await fetchArticlePage(originalUrl);
    const extracted = extractArticleContent(html, originalUrl);
    content = extracted.content;
    image = image || extracted.image;
  } catch (err) {
    console.warn(`[Crawler] Không tải được trang gốc (${originalUrl}): ${err.message}`);
  }
  if (!content) content = item.contentSnippet || item.content || item.rawDescription || "";
  content = content.replace(/<[^>]*>/g, " ").replace(/[ \t]+/g, " ").trim();
  if (!content) content = title;

  // 3. Gemini AI (hoặc fallback)
  const ai = await analyzeArticle({
    title,
    content,
    source_name: feed.source_name,
    category_hint: feed.category_hint,
    feed_url: feed.url,
  });

  const parsedDate = new Date(item.isoDate || item.pubDate || Date.now());
  const publishedAt = Number.isNaN(parsedDate.getTime()) ? new Date() : parsedDate;

  // 4. Lưu MongoDB
  try {
    await Article.create({
      article_id: makeArticleId(originalUrl),
      title,
      main_grid_summary: ai.main_grid_summary,
      card_summary: ai.card_summary,
      full_content: content,
      image_url: image,
      original_url: originalUrl,
      source_name: feed.source_name,
      category: ai.category,
      published_at: publishedAt,
      date_label: buildDateLabel(publishedAt),
      ai_score: ai.ai_score,
      is_main_featured: ai.is_main_featured,
    });
  } catch (err) {
    if (err && err.code === 11000) return "duplicate"; // 2 lần quét chạy song song
    throw err;
  }

  return ai.ai_used ? "saved_ai" : "saved";
}

// ---------------------------------------------------------------------------
// Quét 1 feed
// ---------------------------------------------------------------------------
async function crawlFeed(feed, { maxNewItems, geminiDelayMs }) {
  const result = {
    source_name: feed.source_name,
    feed_url: feed.url,
    fetched: 0, // bài mới đã lưu
    ai_processed: 0,
    duplicates: 0,
    errors: [],
    status: "SUCCESS",
  };

  let rss;
  try {
    rss = await parser.parseURL(feed.url);
  } catch (err) {
    result.status = "FAILED";
    result.errors.push(`Không đọc được RSS: ${err.message}`);
    return result;
  }

  for (const item of rss.items || []) {
    if (result.fetched >= maxNewItems) break;
    try {
      const outcome = await processFeedItem(item, feed);
      if (outcome === "duplicate") result.duplicates += 1;
      if (outcome === "saved" || outcome === "saved_ai") result.fetched += 1;
      if (outcome === "saved_ai") {
        result.ai_processed += 1;
        if (geminiDelayMs > 0) await sleep(geminiDelayMs); // tránh vượt giới hạn request/phút của Gemini
      }
    } catch (err) {
      result.errors.push(`${item.link || item.title}: ${err.message}`);
    }
  }

  if (result.errors.length > 0) {
    result.status = result.fetched > 0 || result.duplicates > 0 ? "PARTIAL" : "FAILED";
  }
  return result;
}

async function writeCrawlLog(result) {
  try {
    await CrawlLog.create({
      source_name: result.source_name,
      feed_url: result.feed_url,
      articles_fetched: result.fetched,
      articles_processed_by_ai: result.ai_processed,
      status: result.status,
      error_message: result.errors.length ? result.errors.slice(0, 5).join(" | ").slice(0, 1000) : null,
      executed_at: new Date(),
    });
  } catch (err) {
    console.warn("[Crawler] Không ghi được crawl_logs:", err.message);
  }
}

// ---------------------------------------------------------------------------
// Quét toàn bộ feed
// ---------------------------------------------------------------------------
function isCrawling() {
  return crawling;
}

/**
 * @param {{maxNewItemsPerFeed?: number}} [options]
 */
async function crawlAllFeeds(options = {}) {
  if (crawling) return { skipped: true, reason: "Một lượt quét khác đang chạy" };
  crawling = true;

  const maxNewItems = options.maxNewItemsPerFeed ?? envInt("MAX_ITEMS_PER_FEED", 3);
  const geminiDelayMs = isGeminiConfigured() ? envInt("GEMINI_DELAY_MS", 6500) : 0;
  const startedAt = new Date();
  const feeds = [];

  console.log(
    `[Crawler] Bắt đầu quét ${FEEDS.length} nguồn (tối đa ${maxNewItems} bài mới/nguồn, ` +
      `${isGeminiConfigured() ? "có Gemini" : "KHÔNG có GEMINI_API_KEY → chế độ fallback"})`
  );

  try {
    for (const feed of FEEDS) {
      const result = await crawlFeed(feed, { maxNewItems, geminiDelayMs });
      await writeCrawlLog(result);
      feeds.push(result);
      console.log(
        `[Crawler] ${result.source_name} | ${feed.url} → ${result.status}: +${result.fetched} bài ` +
          `(AI: ${result.ai_processed}, trùng: ${result.duplicates}${result.errors.length ? `, lỗi: ${result.errors.length}` : ""})`
      );
    }
  } finally {
    crawling = false;
  }

  const totals = feeds.reduce(
    (acc, f) => ({
      saved: acc.saved + f.fetched,
      ai_processed: acc.ai_processed + f.ai_processed,
      duplicates: acc.duplicates + f.duplicates,
      failed_feeds: acc.failed_feeds + (f.status === "FAILED" ? 1 : 0),
    }),
    { saved: 0, ai_processed: 0, duplicates: 0, failed_feeds: 0 }
  );

  return { started_at: startedAt, finished_at: new Date(), totals, feeds };
}

module.exports = {
  FEEDS,
  crawlAllFeeds,
  isCrawling,
  // xuất thêm để dễ kiểm thử
  _internal: { extractArticleContent, extractImageFromRssItem, normalizeUrl, makeArticleId },
};
