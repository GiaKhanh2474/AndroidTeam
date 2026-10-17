package com.example.news.ui.common

import java.util.Locale

/**
 * Ngôn ngữ dùng để ĐỊNH DẠNG SỐ và NGÀY.
 *
 * Toàn bộ chữ trong ứng dụng hiện là tiếng Việt viết cứng (chưa có bản dịch nào), nên số
 * cũng phải theo cách viết của tiếng Việt cho khớp: "26.715" và "2,4%" — chứ không phải
 * "26,715" và "2.4%" như cách viết tiếng Anh. Nếu lấy theo ngôn ngữ của máy thì một người
 * dùng đặt máy bằng tiếng Anh sẽ thấy chữ tiếng Việt đi kèm số kiểu tiếng Anh, trông rất
 * lệch.
 *
 * KHI LÀM ĐA NGÔN NGỮ: đổi hằng này thành `Locale.getDefault()` để số liệu theo ngôn ngữ
 * của máy, đồng thời chuyển toàn bộ chuỗi viết cứng trong mã sang `strings.xml`.
 */
val APP_LOCALE: Locale = Locale.forLanguageTag("vi-VN")
