package com.example.news.data.mock

import com.example.news.data.model.Article
import com.example.news.data.model.Category
import com.example.news.data.model.CategoryWithArticles

/**
 * Dữ liệu MINH HOẠ.
 *
 * Đây KHÔNG phải tin thật. Chưa có nguồn tin/API thật nào được chốt cho dự án, nên:
 *  - tên nguồn để là "Bản tin minh hoạ", không mượn tên toà soạn thật để tránh ngụ ý
 *    rằng đây là bài viết có thật của họ;
 *  - đường dẫn dùng example.com (tên miền dành riêng cho ví dụ, không trỏ tới đâu);
 *  - giao diện có nhãn "Dữ liệu minh hoạ".
 *
 * Khi chốt được API thật chỉ cần thay [MockNewsRepository] bằng bản Retrofit, phần UI
 * và ViewModel không phải sửa.
 */
object MockNews {

    private const val MINUTE_MS = 60L * 1000
    private const val DAY_MS = 24L * 60 * MINUTE_MS

    val categories: List<Category> = listOf(
        Category(id = "kinh-te", name = "Kinh tế"),
        Category(id = "chinh-tri", name = "Chính trị"),
        Category(id = "cong-nghe", name = "Công nghệ"),
    )

    /** Mốc thời gian gốc, tính một lần để mọi bài luôn sắp xếp ổn định trong một phiên chạy. */
    private val now: Long = System.currentTimeMillis()

    private val articles: List<Article> = listOf(
        // ---------------- Kinh tế ----------------
        article(
            id = "kt-01",
            categoryId = "kinh-te",
            title = "Tăng trưởng quý này vượt dự báo",
            summary = "Mức tăng cao hơn kỳ vọng nhờ sản xuất công nghiệp và xuất khẩu phục hồi rõ nét.",
            daysAgo = 0,
            minuteOffset = 5,
        ),
        article(
            id = "kt-02",
            categoryId = "kinh-te",
            title = "Lãi suất điều hành được giữ nguyên",
            summary = "Quyết định giữ nguyên nhằm ổn định mặt bằng lãi suất trong ngắn hạn.",
            daysAgo = 0,
            minuteOffset = 20,
        ),
        article(
            id = "kt-03",
            categoryId = "kinh-te",
            title = "Xuất khẩu nông sản lập mốc mới",
            summary = "Nhiều mặt hàng chủ lực ghi nhận khối lượng xuất khẩu cao nhất từ trước tới nay.",
            daysAgo = 1,
            minuteOffset = 10,
        ),
        article(
            id = "kt-04",
            categoryId = "kinh-te",
            title = "Giá xăng dầu trong nước được điều chỉnh",
            summary = "Kỳ điều hành mới ghi nhận mức thay đổi nhẹ so với kỳ liền trước.",
            daysAgo = 1,
            minuteOffset = 30,
        ),
        article(
            id = "kt-05",
            categoryId = "kinh-te",
            title = "Doanh nghiệp nhỏ và vừa tiếp cận vốn dễ hơn",
            summary = "Thủ tục cho vay được rút gọn, thời gian xét duyệt giảm đáng kể.",
            daysAgo = 2,
            minuteOffset = 15,
        ),
        article(
            id = "kt-06",
            categoryId = "kinh-te",
            title = "Bán lẻ trực tuyến mở rộng nhanh",
            summary = "Kênh bán hàng trực tuyến tiếp tục là động lực tăng trưởng chính của ngành bán lẻ.",
            daysAgo = 2,
            minuteOffset = 40,
        ),

        // ---------------- Chính trị ----------------
        article(
            id = "ct-01",
            categoryId = "chinh-tri",
            title = "Kỳ họp thường niên bàn về ngân sách",
            summary = "Các nội dung trọng tâm xoay quanh phân bổ ngân sách và ưu tiên đầu tư công.",
            daysAgo = 0,
            minuteOffset = 8,
        ),
        article(
            id = "ct-02",
            categoryId = "chinh-tri",
            title = "Cải cách thủ tục hành chính được đẩy mạnh",
            summary = "Nhiều thủ tục được cắt giảm, phần lớn hồ sơ có thể nộp trực tuyến.",
            daysAgo = 0,
            minuteOffset = 25,
        ),
        article(
            id = "ct-03",
            categoryId = "chinh-tri",
            title = "Hợp tác khu vực tiếp tục được củng cố",
            summary = "Các bên nhất trí mở rộng hợp tác về thương mại và hạ tầng kết nối.",
            daysAgo = 1,
            minuteOffset = 5,
        ),
        article(
            id = "ct-04",
            categoryId = "chinh-tri",
            title = "Chính sách an sinh giai đoạn mới",
            summary = "Ưu tiên mở rộng diện bao phủ và nâng mức hỗ trợ cho nhóm yếu thế.",
            daysAgo = 1,
            minuteOffset = 35,
        ),
        article(
            id = "ct-05",
            categoryId = "chinh-tri",
            title = "Đối thoại về ứng phó biến đổi khí hậu",
            summary = "Các cam kết giảm phát thải được rà soát lại theo tiến độ thực tế.",
            daysAgo = 2,
            minuteOffset = 12,
        ),
        article(
            id = "ct-06",
            categoryId = "chinh-tri",
            title = "Chương trình phát triển hạ tầng giao thông",
            summary = "Danh mục dự án trọng điểm được điều chỉnh để bảo đảm tiến độ giải ngân.",
            daysAgo = 2,
            minuteOffset = 45,
        ),

        // ---------------- Công nghệ ----------------
        article(
            id = "cn-01",
            categoryId = "cong-nghe",
            title = "Trung tâm dữ liệu xanh đi vào hoạt động",
            summary = "Cơ sở mới sử dụng hệ thống làm mát tiết kiệm điện và nguồn năng lượng tái tạo.",
            daysAgo = 0,
            minuteOffset = 12,
        ),
        article(
            id = "cn-02",
            categoryId = "cong-nghe",
            title = "Chip xử lý AI tiết kiệm điện hơn",
            summary = "Thế hệ chip mới giảm đáng kể điện năng tiêu thụ cho cùng khối lượng tính toán.",
            daysAgo = 0,
            minuteOffset = 28,
        ),
        article(
            id = "cn-03",
            categoryId = "cong-nghe",
            title = "An ninh mạng thành ưu tiên của doanh nghiệp",
            summary = "Nhiều doanh nghiệp tăng ngân sách cho bảo mật sau các sự cố gần đây.",
            daysAgo = 1,
            minuteOffset = 18,
        ),
        article(
            id = "cn-04",
            categoryId = "cong-nghe",
            title = "Điện toán lượng tử tiến thêm một bước",
            summary = "Số lượng qubit ổn định tăng lên, mở ra khả năng thử nghiệm bài toán lớn hơn.",
            daysAgo = 1,
            minuteOffset = 42,
        ),
        article(
            id = "cn-05",
            categoryId = "cong-nghe",
            title = "Thiết bị đeo theo dõi sức khoẻ phổ biến hơn",
            summary = "Cảm biến mới cho phép theo dõi liên tục nhiều chỉ số sức khoẻ quan trọng.",
            daysAgo = 2,
            minuteOffset = 22,
        ),
        article(
            id = "cn-06",
            categoryId = "cong-nghe",
            title = "Mã nguồn mở được ứng dụng rộng rãi",
            summary = "Nhiều tổ chức chuyển sang dùng nền tảng mã nguồn mở để giảm phụ thuộc.",
            daysAgo = 2,
            minuteOffset = 50,
        ),
    )

    /** Toàn bộ bài báo, mới nhất trước. */
    fun allArticles(): List<Article> = articles.sortedByDescending { it.publishedAt }

    /** Bài báo của một chuyên mục, mới nhất trước. */
    fun articlesOf(categoryId: String): List<Article> =
        articles.filter { it.categoryId == categoryId }.sortedByDescending { it.publishedAt }

    /** Nội dung cho màn Main: mỗi chuyên mục kèm [perCategory] bài tóm tắt đầu tiên. */
    fun mainFeed(perCategory: Int = 3): List<CategoryWithArticles> =
        categories.map { category ->
            CategoryWithArticles(
                category = category,
                articles = articlesOf(category.id).take(perCategory),
            )
        }

    fun articleById(articleId: String): Article? = articles.firstOrNull { it.id == articleId }

    private fun article(
        id: String,
        categoryId: String,
        title: String,
        summary: String,
        daysAgo: Int,
        minuteOffset: Int,
    ): Article = Article(
        id = id,
        categoryId = categoryId,
        title = title,
        summary = summary,
        source = SOURCE,
        publishedAt = now - daysAgo * DAY_MS - minuteOffset * MINUTE_MS,
        sourceUrl = "https://example.com/bai-viet/$id",
        imageUrl = null,
        content = "Đây là nội dung minh hoạ cho bài \"$title\". " +
            "Bản demo chưa kết nối nguồn tin thật nên toàn bộ tiêu đề, tóm tắt và nội dung " +
            "trong ứng dụng chỉ nhằm kiểm chứng giao diện và luồng điều hướng.",
    )

    const val SOURCE = "Bản tin minh hoạ"
}
