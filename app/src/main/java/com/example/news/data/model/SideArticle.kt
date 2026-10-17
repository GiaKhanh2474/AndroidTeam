package com.example.news.data.model

/**
 * Loại bài báo bên lề ở cuối trang Thị trường.
 *
 * Đặc tả chia phần này thành 3 nhóm cố định, mỗi nhóm một kiểu trình bày.
 */
enum class SideArticleKind {
    /** Dòng sự kiện tin vắn toàn cầu cập nhật nhanh. */
    NEWS_TICKER,

    /** Bảng xếp hạng các bài báo nóng trong ngày. */
    TOP_READ,

    /** Góc phân tích / khám phá mở rộng. */
    ANALYSIS,
}

/**
 * Bài báo bên lề: KHÔNG lưu và KHÔNG hiện lên danh sách chính ở trang Main (theo đặc tả),
 * nên nó là một kiểu dữ liệu riêng chứ không phải [Article].
 */
data class SideArticle(
    val id: String,
    val kind: SideArticleKind,
    val title: String,
    val summary: String,
)
