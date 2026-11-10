package com.vlu.newsapi.data.model

/**
 * Mã chuyên mục dùng chung giữa màn Main và nguồn dữ liệu.
 *
 * Đặc tả ghi rõ ba mã `"KINH_TE"`, `"CHINH_TRI"`, `"CONG_NGHE"` khi mở trang Chuyên mục. Gom
 * vào một chỗ để màn Main và dữ liệu minh hoạ không thể lệch nhau vì một bên gõ sai.
 */
object CategoryIds {
    const val ECONOMY = "KINH_TE"
    const val POLITICS = "CHINH_TRI"
    const val TECHNOLOGY = "CONG_NGHE"
}
