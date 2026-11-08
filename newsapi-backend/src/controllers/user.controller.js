const User = require("../models/User.model");
const { normalizeEmail, normalizePhone } = require("../utils/identity");

const OUTPUT_PROJECTION = {
  _id: 0,
  firebase_uid: 1,
  email: 1,
  phone: 1,
  display_name: 1,
  avatar_url: 1,
  account_tier: 1,
  starred_top_symbol: 1,
  saved_chart_symbols: 1,
  saved_article_ids: 1,
};

const MESSAGES = {
  email: "Email này đã được sử dụng",
  phone: "Số điện thoại này đã được sử dụng",
  both: "Email và số điện thoại này đã được sử dụng",
};

function optionalString(value) {
  return typeof value === "string" && value.trim() ? value.trim() : undefined;
}

function isBlank(value) {
  return value === undefined || value === null || (typeof value === "string" && value.trim() === "");
}

function badRequest(res, message) {
  return res.status(400).json({ status: "error", message });
}

/** Đọc + chuẩn hóa email / phone từ body. Trả về { email?, phone? } hoặc { error } */
function parseIdentity(body) {
  const result = {};
  if (!isBlank(body.email)) {
    const email = normalizeEmail(body.email);
    if (!email) return { error: "Email không hợp lệ" };
    result.email = email;
  }
  if (!isBlank(body.phone)) {
    const phone = normalizePhone(body.phone);
    if (!phone) return { error: "Số điện thoại không hợp lệ" };
    result.phone = phone;
  }
  return result;
}

/**
 * Tìm các giá trị đang bị TÀI KHOẢN KHÁC dùng.
 * @returns {Promise<Array<"email"|"phone">>}
 */
async function findConflictFields(firebaseUid, { email, phone }) {
  const or = [];
  if (email) or.push({ email });
  if (phone) or.push({ phone });
  if (or.length === 0) return [];

  const filter = { $or: or };
  if (firebaseUid) filter.firebase_uid = { $ne: firebaseUid };

  const docs = await User.find(filter, { _id: 0, email: 1, phone: 1 }).lean();
  const found = new Set();
  for (const doc of docs) {
    if (email && doc.email === email) found.add("email");
    if (phone && doc.phone === phone) found.add("phone");
  }
  return ["email", "phone"].filter((f) => found.has(f));
}

function describeConflict(fields) {
  if (fields.length === 2) return { code: "EMAIL_AND_PHONE_ALREADY_IN_USE", message: MESSAGES.both };
  if (fields[0] === "email") return { code: "EMAIL_ALREADY_IN_USE", message: MESSAGES.email };
  return { code: "PHONE_ALREADY_IN_USE", message: MESSAGES.phone };
}

function sendConflict(res, fields) {
  const { code, message } = describeConflict(fields);
  return res.status(409).json({ status: "error", code, fields, message });
}

function upsertUser(firebaseUid, update) {
  return User.findOneAndUpdate({ firebase_uid: firebaseUid }, update, {
    upsert: true,
    new: true,
    setDefaultsOnInsert: true,
    runValidators: true,
    projection: OUTPUT_PROJECTION,
  }).lean();
}

// ---------------------------------------------------------------------------
// POST /api/v1/users/check — kiểm tra email / số điện thoại đã được dùng chưa
// Gọi TRƯỚC khi tạo tài khoản Firebase để không sinh ra tài khoản Firebase "mồ côi".
// Body: { email?, phone? }  (cần ít nhất email hoặc phone)
// 200: { available, email_in_use, phone_in_use, message }   (message = null nếu dùng được)
// ---------------------------------------------------------------------------
async function checkUser(req, res) {
  const body = req.body || {};
  const { email, phone, error } = parseIdentity(body);
  if (error) return badRequest(res, error);
  if (!email && !phone) return badRequest(res, "Cần gửi ít nhất email hoặc số điện thoại");

  // Public pre-registration checks must not exclude an identity supplied by the caller.
  const fields = await findConflictFields(null, { email, phone });
  const inUse = new Set(fields);

  res.json({
    available: fields.length === 0,
    email_in_use: inUse.has("email"),
    phone_in_use: inUse.has("phone"),
    message: fields.length ? describeConflict(fields).message : null,
  });
}

// ---------------------------------------------------------------------------
// POST /api/v1/users/sync — đồng bộ tài khoản Firebase sau khi đăng ký / đăng nhập
// Authorization: Bearer <Firebase ID token>; body: { email?, phone?, display_name?, avatar_url? }
//  - Tài khoản MỚI phải có email hoặc phone.
//  - Email / phone đã thuộc tài khoản khác → 409 (EMAIL_ALREADY_IN_USE, PHONE_ALREADY_IN_USE, ...).
// ---------------------------------------------------------------------------
async function syncUser(req, res) {
  const body = req.body || {};
  const firebaseUid = req.auth?.uid;
  if (!firebaseUid) return res.status(401).json({ status: "error", message: "Cần Firebase ID token" });
  if (body.firebase_uid !== undefined && body.firebase_uid !== firebaseUid) {
    return res.status(403).json({ status: "error", message: "firebase_uid không khớp tài khoản đăng nhập" });
  }

  const { email, phone, error } = parseIdentity(body);
  if (error) return badRequest(res, error);

  const alreadyExists = Boolean(await User.exists({ firebase_uid: firebaseUid }));
  if (!alreadyExists && !email && !phone) {
    return badRequest(res, "Cần có email hoặc số điện thoại để tạo tài khoản");
  }

  const conflicts = await findConflictFields(firebaseUid, { email, phone });
  if (conflicts.length) return sendConflict(res, conflicts);

  // Chỉ ghi đè các trường Android thực sự gửi lên (không xóa dữ liệu cũ bằng giá trị rỗng)
  const set = {};
  if (email) set.email = email;
  if (phone) set.phone = phone;
  const displayName = optionalString(body.display_name);
  const avatarUrl = optionalString(body.avatar_url);
  if (displayName !== undefined) set.display_name = displayName;
  if (avatarUrl !== undefined) set.avatar_url = avatarUrl;
  const update = Object.keys(set).length ? { $set: set } : {};

  let user;
  try {
    user = await upsertUser(firebaseUid, update);
  } catch (err) {
    if (!err || err.code !== 11000) throw err;

    // Unique index chặn trường hợp 2 yêu cầu cùng lúc (đua nhau) vượt qua bước kiểm tra phía trên
    const keys = Object.keys(err.keyPattern || {});
    const conflictFields = ["email", "phone"].filter((f) => keys.includes(f));
    if (conflictFields.length) return sendConflict(res, conflictFields);

    // Trùng firebase_uid do 2 lần upsert đồng thời → thử lại 1 lần
    user = await upsertUser(firebaseUid, update);
  }

  res.json(user);
}

module.exports = { syncUser, checkUser };
