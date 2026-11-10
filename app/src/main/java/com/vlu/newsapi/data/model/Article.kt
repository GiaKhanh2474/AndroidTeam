package com.vlu.newsapi.data.model

/**
 * Một bài báo.
 *
 * @param publishedAt thời điểm đăng, tính bằng epoch milliseconds — dùng để nhóm bài
 *   theo ngày ở trang Chuyên mục.
 * @param sourceUrl link ra trang báo gốc (đặc tả yêu cầu link nằm ngay dưới nguồn báo).
 * @param imageUrl  URL ảnh lớn của bài, trang Chi tiết tải bằng Glide. Null nghĩa là bài không
 *   có ảnh; khi đó màn hình dùng ảnh giữ chỗ.
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
