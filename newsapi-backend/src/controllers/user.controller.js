const User = require("../models/User.model");

const OUTPUT_PROJECTION = {
  _id: 0,
  firebase_uid: 1,
  email: 1,
  display_name: 1,
  avatar_url: 1,
  account_tier: 1,
  starred_top_symbol: 1,
  saved_chart_symbols: 1,
  saved_article_ids: 1,
};

function optionalString(value) {
  return typeof value === "string" && value.trim() ? value.trim() : undefined;
}

// ---------------------------------------------------------------------------
// API 7a — POST /api/v1/users/sync
// Body: { firebase_uid, email, display_name, avatar_url }
// ---------------------------------------------------------------------------
async function syncUser(req, res) {
  const body = req.body || {};
  const firebaseUid = optionalString(body.firebase_uid);
  if (!firebaseUid) {
    return res.status(400).json({ status: "error", message: "Thiếu firebase_uid" });
  }

  // Chỉ ghi đè các trường Android thực sự gửi lên (không xóa dữ liệu cũ bằng giá trị rỗng)
  const set = {};
  const email = optionalString(body.email);
  const displayName = optionalString(body.display_name);
  const avatarUrl = optionalString(body.avatar_url);
  if (email !== undefined) set.email = email;
  if (displayName !== undefined) set.display_name = displayName;
  if (avatarUrl !== undefined) set.avatar_url = avatarUrl;

  const update = Object.keys(set).length ? { $set: set } : {};

  const user = await User.findOneAndUpdate({ firebase_uid: firebaseUid }, update, {
    upsert: true,
    new: true,
    setDefaultsOnInsert: true,
    runValidators: true,
    projection: OUTPUT_PROJECTION,
  }).lean();

  res.json(user);
}

module.exports = { syncUser };
