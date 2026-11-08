const { GoogleGenAI } = require("@google/genai");

/**
 * Khởi tạo Gemini client (lazy) — đọc biến môi trường tại thời điểm gọi,
 * nên không phụ thuộc thứ tự nạp dotenv.
 */
let client = null;
let clientKey = null;

function getApiKey() {
  const key = (process.env.GEMINI_API_KEY || "").trim();
  if (!key || /^your_/i.test(key)) return null; // chưa cấu hình hoặc vẫn là giá trị mẫu
  return key;
}

function isGeminiConfigured() {
  return getApiKey() !== null;
}

function getModelName() {
  return (process.env.GEMINI_MODEL || "gemini-3.8-flash").trim();
}

/** @returns {GoogleGenAI|null} null nếu chưa có API key hợp lệ (service sẽ tự chạy chế độ fallback) */
function getGeminiClient() {
  const key = getApiKey();
  if (!key) return null;
  if (!client || clientKey !== key) {
    client = new GoogleGenAI({ apiKey: key });
    clientKey = key;
  }
  return client;
}

module.exports = { getGeminiClient, isGeminiConfigured, getModelName };
