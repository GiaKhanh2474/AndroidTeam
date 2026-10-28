const MarketChart = require("../models/MarketChart.model");

// MarketAssetChartDto — đúng các trường Android đang chờ (không gồm is_default_top / display_order)
const PROJECTION = {
  _id: 0,
  symbol: 1,
  asset_name: 1,
  asset_type: 1,
  unit: 1,
  current_value: 1,
  change_percent: 1,
  history_points: 1,
};

// ---------------------------------------------------------------------------
// API 5 — GET /api/v1/market/charts
// ---------------------------------------------------------------------------
async function getMarketCharts(req, res) {
  const charts = await MarketChart.find({}, PROJECTION)
    .sort({ is_default_top: -1, display_order: 1 })
    .lean();

  res.json(
    charts.map((c) => ({
      symbol: c.symbol,
      asset_name: c.asset_name,
      asset_type: c.asset_type,
      unit: c.unit,
      current_value: c.current_value,
      change_percent: c.change_percent,
      history_points: (c.history_points || []).map((p) => ({
        recorded_at: p.recorded_at,
        price_value: p.price_value,
      })),
    }))
  );
}

module.exports = { getMarketCharts };
