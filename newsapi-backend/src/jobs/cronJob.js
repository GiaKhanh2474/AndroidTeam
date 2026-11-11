require("dotenv").config();
const cron = require("node-cron");

const { crawlAllFeeds, isCrawling } = require("../services/rssCrawler.service");
const { updateMarketCharts } = require("../services/marketCrawler.service");

const { retryPendingArticles } = require("../services/aiRetry.service");
const { createCycleRunner } = require("./cycleRunner");
const { runFullCycle, isCycleRunning } = createCycleRunner({
  market: updateMarketCharts, retryAi: retryPendingArticles, news: crawlAllFeeds, rssBusy: isCrawling,
});

/** Lập lịch chạy định kỳ (mặc định mỗi 30 phút, múi giờ Việt Nam). */
function startCronJobs() {
  if (String(process.env.CRON_ENABLED || "true").toLowerCase() === "false") {
    console.log("[Cron] Đã tắt (CRON_ENABLED=false).");
    return null;
  }

  const schedule = (process.env.CRON_SCHEDULE || "*/30 * * * *").trim();
  if (!cron.validate(schedule)) {
    throw new Error(`CRON_SCHEDULE không hợp lệ: "${schedule}"`);
  }

  const task = cron.schedule(
    schedule,
    async () => {
      if (isCycleRunning()) {
        console.log("[Cron] Bỏ qua — lượt quét trước vẫn đang chạy.");
        return;
      }
      console.log(`[Cron] ${new Date().toISOString()} — bắt đầu chu kỳ quét tin & cập nhật biểu đồ`);
      await runFullCycle();
    },
    { timezone: "Asia/Ho_Chi_Minh" }
  );

  console.log(`[Cron] Đã lập lịch: "${schedule}" (Asia/Ho_Chi_Minh)`);

  if (String(process.env.CRON_RUN_ON_START || "false").toLowerCase() === "true") {
    runFullCycle().catch((err) => console.error("[Cron] Lượt chạy đầu tiên thất bại:", err.message));
  }
  return task;
}

module.exports = { startCronJobs, runFullCycle, isCycleRunning };

// Chạy 1 lần từ dòng lệnh: npm run crawl
if (require.main === module) {
  const connectDB = require("../config/db");
  const mongoose = require("mongoose");
  connectDB()
    .then(() => runFullCycle())
    .then((result) => {
      console.log(JSON.stringify(result, null, 2));
      return mongoose.disconnect();
    })
    .catch(async (err) => {
      console.error("Thất bại:", err.message);
      await mongoose.disconnect().catch(() => {});
      process.exitCode = 1;
    });
}
