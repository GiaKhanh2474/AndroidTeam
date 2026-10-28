const mongoose = require("mongoose");
const Article = require("../models/Article.model");

const VALID_CATEGORIES = ["KINH_TE", "CHINH_TRI", "CONG_NGHE", "BEN_LE"];

// Thứ tự trường trong JSON khớp ArticleDto của Android
const DTO_FIELDS = [
  "article_id",
  "title",
  "main_grid_summary",
  "card_summary",
  "full_content",
  "image_url",
  "original_url",
  "source_name",
  "category",
  "published_at",
  "date_label",
  "ai_score",
  "is_main_featured",
];

/** Projection MongoDB: bỏ _id; có thể bỏ full_content để danh sách nhẹ hơn */
function projection({ withContent }) {
  const p = { _id: 0 };
  for (const f of DTO_FIELDS) {
    if (f === "full_content" && !withContent) continue;
    p[f] = 1;
  }
  return p;
}

const WITH_CONTENT = projection({ withContent: true });
const WITHOUT_CONTENT = projection({ withContent: false });

/** Chuyển document sang ArticleDto (tên trường snake_case, giữ nguyên 100% so với đặc tả) */
function toArticleDto(doc, { includeContent = true } = {}) {
  return {
    article_id: doc.article_id,
    title: doc.title,
    main_grid_summary: doc.main_grid_summary,
    card_summary: doc.card_summary,
    full_content: includeContent && doc.full_content != null ? doc.full_content : null,
    image_url: doc.image_url != null ? doc.image_url : null,
    original_url: doc.original_url,
    source_name: doc.source_name,
    category: doc.category,
    published_at: doc.published_at,
    date_label: doc.date_label,
    ai_score: doc.ai_score,
    is_main_featured: doc.is_main_featured === true,
  };
}

function sendError(res, status, message) {
  return res.status(status).json({ status: "error", message });
}

function escapeRegex(text) {
  return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function parsePositiveInt(value, fallback, max) {
  const n = parseInt(value, 10);
  if (!Number.isFinite(n) || n < 1) return fallback;
  return Math.min(n, max);
}

// ---------------------------------------------------------------------------
// API 1 — GET /api/v1/news/main-grids
//
// Đặc tả sắp xếp theo { is_main_featured, ai_score, published_at }. Nếu chỉ sắp xếp như vậy thì
// bài cũ điểm cao sẽ chiếm ô lưới 3x3 mãi mãi dù crawler liên tục thêm tin mới. Vì vậy mỗi chuyên mục
// ưu tiên các bài trong khoảng MAIN_GRID_WINDOW_HOURS giờ gần nhất (mặc định 48 giờ), rồi mới bổ sung
// bài cũ hơn nếu chưa đủ 3 bài — lưới luôn đầy và luôn ưu tiên tin mới.
// ---------------------------------------------------------------------------
const MAIN_GRID_LIMIT = 3;
const MAIN_GRID_SORT = { is_main_featured: -1, ai_score: -1, published_at: -1 };

function mainGridWindowMs() {
  const hours = parseInt(process.env.MAIN_GRID_WINDOW_HOURS, 10);
  return (Number.isFinite(hours) && hours > 0 ? hours : 48) * 60 * 60 * 1000;
}

async function topArticles(category) {
  const since = new Date(Date.now() - mainGridWindowMs());

  const fresh = await Article.find({ category, published_at: { $gte: since } }, WITH_CONTENT)
    .sort(MAIN_GRID_SORT)
    .limit(MAIN_GRID_LIMIT)
    .lean();
  if (fresh.length >= MAIN_GRID_LIMIT) return fresh;

  const older = await Article.find({ category, published_at: { $lt: since } }, WITH_CONTENT)
    .sort(MAIN_GRID_SORT)
    .limit(MAIN_GRID_LIMIT - fresh.length)
    .lean();
  return fresh.concat(older);
}

async function getMainGrids(req, res) {
  const [kinhTe, chinhTri, congNghe] = await Promise.all([
    topArticles("KINH_TE"),
    topArticles("CHINH_TRI"),
    topArticles("CONG_NGHE"),
  ]);

  res.json({
    kinhTe: kinhTe.map((d) => toArticleDto(d)),
    chinhTri: chinhTri.map((d) => toArticleDto(d)),
    congNghe: congNghe.map((d) => toArticleDto(d)),
  });
}

// ---------------------------------------------------------------------------
// API 2 — GET /api/v1/news/by-date?category=KINH_TE&page=1&limit=20
// Phân trang theo SỐ BÀI (limit); các bài được gom nhóm theo date_label, mới nhất lên đầu.
// Một mốc ngày có thể bị tách qua 2 trang liên tiếp — app nên gộp các nhóm trùng date_group.
// ---------------------------------------------------------------------------
async function getArticlesByDate(req, res) {
  const category = String(req.query.category || "").trim().toUpperCase();
  if (!VALID_CATEGORIES.includes(category)) {
    return sendError(res, 400, `Thiếu hoặc sai tham số category. Giá trị hợp lệ: ${VALID_CATEGORIES.join(", ")}`);
  }

  const page = parsePositiveInt(req.query.page, 1, 100000);
  const limit = parsePositiveInt(req.query.limit, 20, 100);

  const docs = await Article.find({ category }, WITHOUT_CONTENT)
    .sort({ published_at: -1 })
    .skip((page - 1) * limit)
    .limit(limit)
    .lean();

  const groups = [];
  const byLabel = new Map();
  for (const doc of docs) {
    let group = byLabel.get(doc.date_label);
    if (!group) {
      group = { date_group: doc.date_label, articles: [] };
      byLabel.set(doc.date_label, group);
      groups.push(group);
    }
    group.articles.push(toArticleDto(doc, { includeContent: false }));
  }

  res.json(groups);
}

// ---------------------------------------------------------------------------
// API 3 — GET /api/v1/news/detail/:id
// ---------------------------------------------------------------------------
async function getArticleDetail(req, res) {
  const id = String(req.params.id || "").trim();
  if (!id) return sendError(res, 400, "Thiếu id bài báo");

  const filter = mongoose.isValidObjectId(id) && /^[a-f0-9]{24}$/i.test(id)
    ? { $or: [{ article_id: id }, { _id: id }] }
    : { article_id: id };

  const doc = await Article.findOne(filter, WITH_CONTENT).lean();
  if (!doc) return sendError(res, 404, "Không tìm thấy bài báo");

  res.json(toArticleDto(doc));
}

// ---------------------------------------------------------------------------
// API 4 — GET /api/v1/news/search?q=keyword
// ---------------------------------------------------------------------------
async function searchArticles(req, res) {
  const q = String(req.query.q || "").trim().slice(0, 100);
  if (!q) return res.json([]);

  // Ưu tiên Text Index (xếp theo độ liên quan, rồi mới nhất)
  let docs = await Article.find(
    { $text: { $search: q } },
    { ...WITH_CONTENT, score: { $meta: "textScore" } }
  )
    .sort({ score: { $meta: "textScore" }, published_at: -1 })
    .limit(20)
    .lean();

  // Text Index chỉ khớp cả từ → nếu không có kết quả, thử khớp một phần bằng regex
  if (docs.length === 0) {
    const regex = new RegExp(escapeRegex(q), "i");
    docs = await Article.find(
      { $or: [{ title: regex }, { card_summary: regex }, { source_name: regex }] },
      WITH_CONTENT
    )
      .sort({ published_at: -1 })
      .limit(20)
      .lean();
  }

  res.json(docs.map((d) => toArticleDto(d)));
}

// ---------------------------------------------------------------------------
// API 6 — GET /api/v1/news/sideline
// ---------------------------------------------------------------------------
async function getSidelineArticles(req, res) {
  const docs = await Article.find({ category: "BEN_LE" }, WITH_CONTENT)
    .sort({ published_at: -1 })
    .limit(15)
    .lean();

  res.json(docs.map((d) => toArticleDto(d)));
}

module.exports = {
  getMainGrids,
  getArticlesByDate,
  getArticleDetail,
  searchArticles,
  getSidelineArticles,
  toArticleDto, // xuất để kiểm thử
};
