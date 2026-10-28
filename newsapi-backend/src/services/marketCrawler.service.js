/**
 * marketCrawler.service.js
 * Cập nhật giá cho collection market_charts (current_value, change_percent, history_points).
 *
 *  - USD_VND   : lấy từ API tỷ giá công khai (open.er-api.com)
 *  - OIL_BRENT : lấy từ Yahoo Finance chart API (BZ=F) — API không chính thức, có thể bị chặn
 *  - Còn lại (RON95, E5_RON92, GOLD_SJC): KHÔNG có API công khai ổn định. Khi MARKET_SIMULATE_FALLBACK != "false",
 *    mỗi ngày giá được mô phỏng dao động nhẹ quanh giá ngày hôm trước — CHỈ để demo, không phải giá thật.
 *
 * Mỗi lần chạy chỉ cập nhật chart ĐÃ CÓ trong DB (chạy `npm run seed` trước).
 * history_points giữ tối đa 30 điểm; điểm của "hôm nay" được ghi đè thay vì thêm mới.
 */
const axios = require("axios");

const MarketChart = require("../models/MarketChart.model");
const { MAX_HISTORY_POINTS } = require("../models/MarketChart.model");
const { buildShortDate } = require("../utils/vnDate");

const HTTP_OPTIONS = {
  timeout: 15000,
  headers: { "User-Agent": "Mozilla/5.0 (compatible; AndroidTeamNewsBot/1.0)" },
};

const round = (value, decimals) => {
  const f = 10 ** decimals;
  return Math.round(value * f) / f;
};

// ---------------------------------------------------------------------------
// Nguồn dữ liệu thật
// ---------------------------------------------------------------------------
const LIVE_PROVIDERS = {
  async USD_VND() {
    const { data } = await axios.get("https://open.er-api.com/v6/latest/USD", HTTP_OPTIONS);
    const rate = data && data.rates && Number(data.rates.VND);
    if (!Number.isFinite(rate) || rate <= 0) throw new Error("API tỷ giá không trả về rates.VND");
    return round(rate, 0);
  },

  async OIL_BRENT() {
    const { data } = await axios.get(
      "https://query1.finance.yahoo.com/v8/finance/chart/BZ=F?interval=1d&range=1d",
      HTTP_OPTIONS
    );
    const price = data && data.chart && data.chart.result && data.chart.result[0] &&
      Number(data.chart.result[0].meta && data.chart.result[0].meta.regularMarketPrice);
    if (!Number.isFinite(price) || price <= 0) throw new Error("Yahoo Finance không trả về giá Brent");
    return round(price, 2);
  },
};

// ---------------------------------------------------------------------------
// Mô phỏng (chỉ cho symbol không có nguồn thật)
// maxMovePct: biên độ dao động tối đa mỗi ngày (%), step: làm tròn theo bước giá
// ---------------------------------------------------------------------------
const SIMULATION = {
  RON95: { maxMovePct: 0.9, step: 10 },
  E5_RON92: { maxMovePct: 0.9, step: 10 },
  GOLD_SJC: { maxMovePct: 0.8, step: 0.1 },
};

function simulateNextValue(symbol, previousValue) {
  const cfg = SIMULATION[symbol];
  if (!cfg) return null;
  const move = (Math.random() * 2 - 1) * (cfg.maxMovePct / 100);
  const raw = previousValue * (1 + move);
  return round(Math.round(raw / cfg.step) * cfg.step, 2);
}

// ---------------------------------------------------------------------------
// Ghi điểm giá vào chart
// ---------------------------------------------------------------------------
function applyPoint(chart, newValue, todayLabel) {
  const points = chart.history_points;
  const last = points[points.length - 1];
  let baseline;

  if (last && last.recorded_at === todayLabel) {
    // Đã có điểm hôm nay → ghi đè; so sánh với điểm của ngày trước đó
    baseline = points.length >= 2 ? points[points.length - 2].price_value : last.price_value;
    last.price_value = newValue;
  } else {
    baseline = last ? last.price_value : newValue;
    points.push({ recorded_at: todayLabel, price_value: newValue });
  }

  while (points.length > MAX_HISTORY_POINTS) points.shift();

  chart.current_value = newValue;
  chart.change_percent = baseline ? round(((newValue - baseline) / baseline) * 100, 2) : 0;
  chart.markModified("history_points");
}

/**
 * @returns {Promise<{updated: string[], skipped: string[], errors: string[]}>}
 */
async function updateMarketCharts() {
  const simulate = String(process.env.MARKET_SIMULATE_FALLBACK || "true").toLowerCase() !== "false";
  const todayLabel = buildShortDate(new Date());
  const summary = { updated: [], skipped: [], errors: [] };

  const charts = await MarketChart.find({});
  if (charts.length === 0) {
    console.warn("[Market] Chưa có biểu đồ nào trong DB — hãy chạy `npm run seed`.");
    return summary;
  }

  for (const chart of charts) {
    const symbol = chart.symbol;
    try {
      let newValue = null;

      if (LIVE_PROVIDERS[symbol]) {
        newValue = await LIVE_PROVIDERS[symbol]();
      } else if (simulate && SIMULATION[symbol]) {
        const last = chart.history_points[chart.history_points.length - 1];
        if (last && last.recorded_at === todayLabel) {
          summary.skipped.push(`${symbol} (đã có điểm hôm nay)`);
          continue; // giá mô phỏng chỉ đổi 1 lần/ngày
        }
        newValue = simulateNextValue(symbol, chart.current_value);
      }

      if (newValue === null) {
        summary.skipped.push(`${symbol} (không có nguồn dữ liệu)`);
        continue;
      }

      applyPoint(chart, newValue, todayLabel);
      await chart.save();
      summary.updated.push(`${symbol}=${newValue}`);
    } catch (err) {
      summary.errors.push(`${symbol}: ${err.message}`);
    }
  }

  console.log(
    `[Market] Đã cập nhật: ${summary.updated.join(", ") || "không có"}` +
      (summary.errors.length ? ` | Lỗi: ${summary.errors.join("; ")}` : "")
  );
  return summary;
}

module.exports = {
  updateMarketCharts,
  _internal: { applyPoint, simulateNextValue },
};
