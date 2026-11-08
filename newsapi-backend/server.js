require("dotenv").config();

const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const mongoose = require("mongoose");

const connectDB = require("./src/config/db");
const { isGeminiConfigured, getModelName } = require("./src/config/gemini");
const apiRoutes = require("./src/routes/api.routes");
const { startCronJobs } = require("./src/jobs/cronJob");

const app = express();

app.disable("x-powered-by");
app.use(cors());
app.use(morgan("dev"));
app.use(express.json({ limit: "1mb" }));

app.get("/", (req, res) => {
  res.json({ name: "AndroidTeamNews API", package: "com.vlu.newsapi", base_url: "/api/v1", health: "/health" });
});

app.get("/health", (req, res) => {
  const dbReady = mongoose.connection.readyState === 1;
  res.status(dbReady ? 200 : 503).json({
    status: dbReady ? "ok" : "db_unavailable",
    gemini: isGeminiConfigured() ? getModelName() : "fallback_mode",
    uptime_seconds: Math.round(process.uptime()),
  });
});

app.use("/api/v1", apiRoutes);

// 404
app.use((req, res) => {
  res.status(404).json({ status: "error", message: `Không tìm thấy đường dẫn ${req.method} ${req.originalUrl}` });
});

// Lỗi chung (4xx/5xx trả về JSON dạng { status: "error", message })
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  if (err.type === "entity.parse.failed") {
    return res.status(400).json({ status: "error", message: "Body JSON không hợp lệ" });
  }
  if (err.name === "ValidationError" || err.name === "CastError") {
    return res.status(400).json({ status: "error", message: err.message });
  }
  console.error("[Server] Lỗi không mong đợi:", err);
  res.status(500).json({ status: "error", message: "Lỗi máy chủ nội bộ" });
});

async function start() {
  try {
    await connectDB();
  } catch (err) {
    console.error("[Server] Không kết nối được MongoDB:", err.message);
    process.exit(1);
  }

  const port = parseInt(process.env.PORT, 10) || 5000;
  const server = app.listen(port, "0.0.0.0", () => {
    console.log(`[Server] AndroidTeamNews API chạy tại http://localhost:${port}/api/v1`);
    console.log(
      isGeminiConfigured()
        ? `[Gemini] Model: ${getModelName()}`
        : "[Gemini] Chưa có GEMINI_API_KEY → dùng chế độ fallback (cắt chuỗi, không dùng AI)."
    );
  });

  startCronJobs();

  const shutdown = async (signal) => {
    console.log(`\n[Server] Nhận ${signal}, đang tắt...`);
    server.close(async () => {
      await mongoose.disconnect().catch(() => {});
      process.exit(0);
    });
    setTimeout(() => process.exit(1), 10000).unref();
  };
  process.on("SIGINT", () => shutdown("SIGINT"));
  process.on("SIGTERM", () => shutdown("SIGTERM"));
}

if (require.main === module) start();

module.exports = app;
