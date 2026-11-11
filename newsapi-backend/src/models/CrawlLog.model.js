const mongoose = require("mongoose");

const { Schema } = mongoose;

const CRAWL_STATUSES = ["SUCCESS", "PARTIAL", "FAILED"];

// Mỗi bản ghi = 1 lần quét 1 nguồn RSS
const CrawlLogSchema = new Schema(
  {
    source_name: { type: String, required: true, trim: true },
    feed_url: { type: String, default: null }, // (bổ sung) biết chính xác feed nào
    articles_fetched: { type: Number, default: 0 }, // số bài MỚI đã lưu vào MongoDB
    articles_processed_by_ai: { type: Number, default: 0 }, // số bài được Gemini xử lý (không tính fallback)
    status: { type: String, enum: CRAWL_STATUSES, required: true },
    error_message: { type: String, default: null },
    executed_at: { type: Date, default: Date.now, index: true },
  },
  {
    collection: "crawl_logs",
    versionKey: false,
  }
);

const CrawlLog = mongoose.models.CrawlLog || mongoose.model("CrawlLog", CrawlLogSchema);

module.exports = CrawlLog;
module.exports.CRAWL_STATUSES = CRAWL_STATUSES;
