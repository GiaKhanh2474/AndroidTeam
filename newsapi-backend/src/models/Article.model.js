const mongoose = require("mongoose");

const { Schema } = mongoose;

const CATEGORIES = ["KINH_TE", "CHINH_TRI", "CONG_NGHE", "BEN_LE"];

const ArticleSchema = new Schema(
  {
    // Mã định danh, VD "art_kt_01" hoặc "art_<hash của URL>" khi do crawler sinh ra
    article_id: { type: String, required: true, unique: true, index: true, trim: true },

    // CHỮ TO — tiêu đề (thẻ ngang + trang Chi tiết)
    title: { type: String, required: true, trim: true },

    // Tóm tắt siêu ngắn 15–20 chữ cho 9 ô của ma trận 3x3
    main_grid_summary: { type: String, required: true },

    // chữ nhỏ 35–50 chữ cho thẻ nằm ngang
    card_summary: { type: String, required: true },

    full_content: { type: String, required: true },

    // Chỉ hiển thị ở trang Chi tiết (Glide)
    image_url: { type: String, default: null },

    // Link bài gốc — đồng thời là khóa chống quét trùng
    original_url: { type: String, required: true, unique: true, trim: true },

    source_name: { type: String, required: true, trim: true },

    category: { type: String, required: true, enum: CATEGORIES },

    published_at: { type: Date, required: true, index: true },

    // "NGÀY DD/MM/YYYY" theo giờ Việt Nam
    date_label: { type: String, required: true, index: true },

    ai_score: { type: Number, default: 50, min: 1, max: 100 },

    is_main_featured: { type: Boolean, default: false, index: true },
  },
  {
    collection: "articles",
    versionKey: false,
  }
);

// 1. Trang Main (API 1) — khớp đúng thứ tự sort của truy vấn
ArticleSchema.index({ category: 1, is_main_featured: -1, ai_score: -1, published_at: -1 });
// 2. Gom nhóm theo ngày (API 2)
ArticleSchema.index({ category: 1, date_label: -1, published_at: -1 });
// 3. Thanh Search (API 4).
//    default_language "none": tắt stemming/stop-words tiếng Anh, phù hợp tiếng Việt.
ArticleSchema.index(
  { title: "text", card_summary: "text", source_name: "text" },
  { name: "ArticleSearchTextIndex", default_language: "none" }
);
// 4. (Bổ sung) Danh sách theo chuyên mục, mới nhất trước — API 2 và API 6 (BEN_LE)
ArticleSchema.index({ category: 1, published_at: -1 });

const Article = mongoose.models.Article || mongoose.model("Article", ArticleSchema);

module.exports = Article;
module.exports.CATEGORIES = CATEGORIES;
