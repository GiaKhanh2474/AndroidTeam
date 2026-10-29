const mongoose = require("mongoose");

const { Schema } = mongoose;

const ACCOUNT_TIERS = ["FREE", "VIP_PRO"];

const UserSchema = new Schema(
  {
    // FirebaseAuth.getInstance().currentUser.uid
    firebase_uid: { type: String, required: true, unique: true, trim: true },

    // Tài khoản cần có email HOẶC số điện thoại (hoặc cả hai). Mỗi giá trị chỉ được dùng cho 1 tài khoản.
    // Email lưu dạng chữ thường; số điện thoại lưu dạng E.164, ví dụ "+84912345678" (xem utils/identity.js).
    email: { type: String, default: null, trim: true, lowercase: true },
    phone: { type: String, default: null, trim: true },

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

// firebase_uid đã có unique index ở khai báo field.
// Unique "một phần": chỉ áp dụng khi trường là chuỗi, nên nhiều tài khoản chỉ có email (phone = null)
// hoặc chỉ có số điện thoại (email = null) vẫn tồn tại song song được.
UserSchema.index({ email: 1 }, { unique: true, partialFilterExpression: { email: { $type: "string" } } });
UserSchema.index({ phone: 1 }, { unique: true, partialFilterExpression: { phone: { $type: "string" } } });

const User = mongoose.models.User || mongoose.model("User", UserSchema);

module.exports = User;
module.exports.ACCOUNT_TIERS = ACCOUNT_TIERS;
