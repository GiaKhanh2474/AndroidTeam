package com.example.news.data.model

/**
 * Một bài báo.
 *
 * @param publishedAt thời điểm đăng, tính bằng epoch milliseconds — dùng để nhóm bài
 *   theo ngày ở trang Chuyên mục.
 * @param sourceUrl link ra trang báo gốc (đặc tả yêu cầu link nằm ngay dưới nguồn báo).
 * @param imageUrl  hiện là null vì các bước mock chưa tải ảnh mạng; ảnh thật sẽ được
 *   nạp bằng Coil ở bước sau, khi đó chỉ cần đổ dữ liệu vào trường này.
 */
data class Article(
    val id: String,
    val categoryId: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: Long,
    val sourceUrl: String,
    val imageUrl: String? = null,
    val content: String = "",
)
