const mongoose = require("mongoose");

const { Schema } = mongoose;

const ASSET_TYPES = ["CURRENCY", "FUEL", "COMMODITY"];
const MAX_HISTORY_POINTS = 30;

// Điểm lịch sử nhúng — recorded_at là chuỗi "DD/MM" để Android dùng thẳng làm nhãn trục X
const HistoryPointSchema = new Schema(
  {
    recorded_at: { type: String, required: true, trim: true },
    price_value: { type: Number, required: true },
  },
  { _id: false }
);

const MarketChartSchema = new Schema(
  {
    // USD_VND | RON95 | E5_RON92 | GOLD_SJC | OIL_BRENT | EUR_VND ...
    symbol: { type: String, required: true, unique: true, uppercase: true, trim: true },
    asset_name: { type: String, required: true, trim: true },
    asset_type: { type: String, required: true, enum: ASSET_TYPES },
    unit: { type: String, required: true, trim: true },

    current_value: { type: Number, required: true },
    change_percent: { type: Number, required: true },

    is_default_top: { type: Boolean, default: false },
    display_order: { type: Number, default: 1 },

    history_points: {
      type: [HistoryPointSchema],
      required: true,
      validate: {
        validator: (arr) => Array.isArray(arr) && arr.length >= 1 && arr.length <= MAX_HISTORY_POINTS,
        message: `history_points phải có từ 1 đến ${MAX_HISTORY_POINTS} điểm`,
      },
    },
  },
  {
    collection: "market_charts",
    versionKey: false,
  }
);

MarketChartSchema.index({ is_default_top: -1, display_order: 1 });

const MarketChart = mongoose.models.MarketChart || mongoose.model("MarketChart", MarketChartSchema);

module.exports = MarketChart;
module.exports.ASSET_TYPES = ASSET_TYPES;
module.exports.MAX_HISTORY_POINTS = MAX_HISTORY_POINTS;
