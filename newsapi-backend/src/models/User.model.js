const mongoose = require("mongoose");

const { Schema } = mongoose;

const ACCOUNT_TIERS = ["FREE", "VIP_PRO"];

const UserSchema = new Schema(
  {
    // FirebaseAuth.getInstance().currentUser.uid
    firebase_uid: { type: String, required: true, unique: true, trim: true },
    email: { type: String, default: null, trim: true, lowercase: true },
    display_name: { type: String, default: null, trim: true },
    avatar_url: { type: String, default: null },

    account_tier: { type: String, enum: ACCOUNT_TIERS, default: "FREE" },

    // symbol biểu đồ đang gắn ngôi sao ưu tiên lên đầu
    starred_top_symbol: { type: String, default: "USD_VND" },
    // mục "Biểu đồ đã lưu" ở trang Avatar
    saved_chart_symbols: { type: [String], default: [] },
    // article_id đã lưu (Double-Tap)
    saved_article_ids: { type: [String], default: [] },
  },
  {
    collection: "users",
    timestamps: true,
    versionKey: false,
  }
);

const User = mongoose.models.User || mongoose.model("User", UserSchema);

module.exports = User;
module.exports.ACCOUNT_TIERS = ACCOUNT_TIERS;
