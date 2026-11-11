/**
 * Tiện ích ngày giờ theo múi giờ Việt Nam (UTC+7).
 * Server có thể chạy ở múi giờ UTC (Render, Railway...), nhưng nhãn ngày hiển thị
 * trên app phải là ngày theo giờ Việt Nam.
 */
const TIME_ZONE = "Asia/Ho_Chi_Minh";

const formatter = new Intl.DateTimeFormat("en-GB", {
  timeZone: TIME_ZONE,
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

function getVnDateParts(date = new Date()) {
  const parts = {};
  for (const p of formatter.formatToParts(date)) parts[p.type] = p.value;
  return { day: parts.day, month: parts.month, year: parts.year };
}

/** "NGÀY 26/10/2026" — dùng cho trường date_label của bài báo */
function buildDateLabel(date = new Date()) {
  const { day, month, year } = getVnDateParts(date);
  return `NGÀY ${day}/${month}/${year}`;
}

/** "26/10" — dùng cho history_points.recorded_at của biểu đồ */
function buildShortDate(date = new Date()) {
  const { day, month } = getVnDateParts(date);
  return `${day}/${month}`;
}

module.exports = { TIME_ZONE, getVnDateParts, buildDateLabel, buildShortDate };
