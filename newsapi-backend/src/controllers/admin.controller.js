const crypto = require("crypto");
const { runFullCycle } = require("../jobs/cronJob");
const { isCrawling } = require("../services/rssCrawler.service");

function isAuthorized(req) {
  const expected = (process.env.ADMIN_KEY || "").trim();
  if (!expected) {
    return process.env.NODE_ENV === "development" && process.env.ADMIN_ALLOW_UNAUTHENTICATED === "true";
  }

  const provided = String(req.get("x-admin-key") || "");
  const a = Buffer.from(provided);
  const b = Buffer.from(expected);
  return a.length === b.length && crypto.timingSafeEqual(a, b);
}

// ---------------------------------------------------------------------------
// API 7b — POST /api/v1/admin/trigger-crawl[?wait=true]
// Mặc định trả 202 ngay và chạy nền (lượt quét có thể mất vài phút vì Gemini giới hạn request/phút).
// ?wait=true: chờ quét xong rồi trả kết quả chi tiết.
// ---------------------------------------------------------------------------
async function triggerCrawl(req, res) {
  if (!isAuthorized(req)) {
    return res.status(401).json({ status: "error", message: "Sai hoặc thiếu header x-admin-key" });
  }
  if (isCrawling()) {
    return res.status(409).json({ status: "error", message: "Một lượt quét khác đang chạy, thử lại sau" });
  }

  const job = runFullCycle();

  if (String(req.query.wait).toLowerCase() === "true") {
    const result = await job;
    return res.json({ status: "completed", ...result });
  }

  job.catch((err) => console.error("[Admin] Lượt quét thất bại:", err.message));
  res.status(202).json({
    status: "started",
    message: "Đã bắt đầu quét RSS + chạy Gemini AI. Xem log server hoặc collection crawl_logs để theo dõi.",
  });
}

module.exports = { triggerCrawl };
