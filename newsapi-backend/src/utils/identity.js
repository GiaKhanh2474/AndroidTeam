/**
 * Chuẩn hóa email / số điện thoại trước khi so sánh trùng và lưu vào database.
 * Chuẩn hóa để "0912 345 678", "+84912345678" và "84912345678" được hiểu là CÙNG MỘT số,
 * và "A@Gmail.com" trùng với "a@gmail.com".
 */
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/** @returns {string|null} email viết thường, hoặc null nếu không hợp lệ */
function normalizeEmail(value) {
  if (typeof value !== "string") return null;
  const email = value.trim().toLowerCase();
  return email.length <= 254 && EMAIL_PATTERN.test(email) ? email : null;
}

/**
 * Đưa số điện thoại về dạng quốc tế E.164, ví dụ "+84912345678".
 * Số bắt đầu bằng 0 được hiểu là số Việt Nam. Số +84 phải là di động Việt Nam (03, 05, 07, 08, 09 + 8 chữ số).
 * @returns {string|null} null nếu không hợp lệ
 */
function normalizePhone(value) {
  if (typeof value !== "string" && typeof value !== "number") return null;

  let s = String(value).trim().replace(/[\s().-]/g, "");
  if (!/^\+?\d+$/.test(s)) return null;

  if (s.startsWith("00")) s = "+" + s.slice(2); // 0084912345678 → +84912345678
  else if (s.startsWith("0")) s = "+84" + s.slice(1); // 0912345678 → +84912345678
  else if (!s.startsWith("+")) {
    if (s.startsWith("84") && s.length >= 11) s = "+" + s; // 84912345678 → +84912345678
    else return null;
  }

  if (s.startsWith("+840")) s = "+84" + s.slice(4); // +840912345678 (gõ thừa số 0) → +84912345678

  if (s.startsWith("+84")) return /^\+84[35789]\d{8}$/.test(s) ? s : null;
  return /^\+[1-9]\d{7,14}$/.test(s) ? s : null;
}

module.exports = { normalizeEmail, normalizePhone };
