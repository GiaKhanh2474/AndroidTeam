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
 * Mỗi chuyên mục có 3 ngày × 3 bài, đủ để trang Chuyên mục chia mốc ngày rõ ràng theo
 * đúng đặc tả ("mỗi ngày 3–5 bài").
 *
 * Khi chốt được API thật chỉ cần thay [MockNewsRepository] bằng bản Retrofit, phần UI
 * và ViewModel không phải sửa.
 */
object MockNews {

    private const val MINUTE_MS = 60L * 1000
    private const val DAY_MS = 24L * 60 * MINUTE_MS

    const val SOURCE = "Bản tin minh hoạ"

    const val CATEGORY_ECONOMY = "kinh-te"
    const val CATEGORY_POLITICS = "chinh-tri"
    const val CATEGORY_TECHNOLOGY = "cong-nghe"

    val categories: List<Category> = listOf(
        Category(id = CATEGORY_ECONOMY, name = "Kinh tế"),
        Category(id = CATEGORY_POLITICS, name = "Chính trị"),
        Category(id = CATEGORY_TECHNOLOGY, name = "Công nghệ"),
    )

    /** Bản nháp một bài báo, chưa gắn chuyên mục và mốc thời gian. */
    private data class Draft(
        val id: String,
        val title: String,
        val summary: String,
        /** 0 = hôm nay, 1 = hôm qua, 2 = hôm kia. */
        val daysAgo: Int,
        /** Lệch thêm vài phút để giờ đăng trông tự nhiên và thứ tự luôn ổn định. */
        val minuteOffset: Int,
    )

    /** Mốc thời gian gốc, tính một lần để mọi bài luôn sắp xếp ổn định trong một phiên chạy. */
    private val now: Long = System.currentTimeMillis()

    private val draftsByCategory: Map<String, List<Draft>> = mapOf(
        CATEGORY_ECONOMY to listOf(
            Draft(
                id = "kt-01",
                title = "Tăng trưởng quý này vượt dự báo",
                summary = "Mức tăng cao hơn kỳ vọng nhờ sản xuất công nghiệp và xuất khẩu phục hồi rõ nét.",
                daysAgo = 0,
                minuteOffset = 5,
            ),
            Draft(
                id = "kt-02",
                title = "Lãi suất điều hành được giữ nguyên",
                summary = "Quyết định giữ nguyên nhằm ổn định mặt bằng lãi suất trong ngắn hạn.",
                daysAgo = 0,
                minuteOffset = 22,
            ),
            Draft(
                id = "kt-03",
                title = "Ngân hàng đẩy mạnh cho vay tiêu dùng",
                summary = "Dư nợ cho vay tiêu dùng tăng trở lại sau nhiều tháng chững lại.",
                daysAgo = 0,
                minuteOffset = 41,
            ),
            Draft(
                id = "kt-04",
                title = "Xuất khẩu nông sản lập mốc mới",
                summary = "Nhiều mặt hàng chủ lực ghi nhận khối lượng xuất khẩu cao nhất từ trước tới nay.",
                daysAgo = 1,
                minuteOffset = 10,
            ),
            Draft(
                id = "kt-05",
                title = "Giá xăng dầu trong nước được điều chỉnh",
                summary = "Kỳ điều hành mới ghi nhận mức thay đổi nhẹ so với kỳ liền trước.",
                daysAgo = 1,
                minuteOffset = 27,
            ),
            Draft(
                id = "kt-06",
                title = "Doanh nghiệp nhỏ và vừa tiếp cận vốn dễ hơn",
                summary = "Thủ tục cho vay được rút gọn, thời gian xét duyệt giảm đáng kể.",
                daysAgo = 1,
                minuteOffset = 48,
            ),
            Draft(
                id = "kt-07",
                title = "Bán lẻ trực tuyến mở rộng nhanh",
                summary = "Kênh bán hàng trực tuyến tiếp tục là động lực tăng trưởng chính của ngành bán lẻ.",
                daysAgo = 2,
                minuteOffset = 14,
            ),
            Draft(
                id = "kt-08",
                title = "Đầu tư công được đẩy nhanh tiến độ",
                summary = "Nhiều dự án trọng điểm giải ngân với khối lượng lớn hơn cùng kỳ.",
                daysAgo = 2,
                minuteOffset = 33,
            ),
            Draft(
                id = "kt-09",
                title = "Thị trường lao động khởi sắc",
                summary = "Nhu cầu tuyển dụng tăng ở nhóm ngành sản xuất và dịch vụ.",
                daysAgo = 2,
                minuteOffset = 52,
            ),
        ),
        CATEGORY_POLITICS to listOf(
            Draft(
                id = "ct-01",
                title = "Kỳ họp thường niên bàn về ngân sách",
                summary = "Các nội dung trọng tâm xoay quanh phân bổ ngân sách và ưu tiên đầu tư công.",
                daysAgo = 0,
                minuteOffset = 8,
            ),
            Draft(
                id = "ct-02",
                title = "Cải cách thủ tục hành chính được đẩy mạnh",
                summary = "Nhiều thủ tục được cắt giảm, phần lớn hồ sơ có thể nộp trực tuyến.",
                daysAgo = 0,
                minuteOffset = 25,
            ),
            Draft(
                id = "ct-03",
                title = "Đối thoại chính sách với doanh nghiệp",
                summary = "Các kiến nghị về thuế và đất đai được đưa ra thảo luận trực tiếp.",
                daysAgo = 0,
                minuteOffset = 44,
            ),
            Draft(
                id = "ct-04",
                title = "Hợp tác khu vực tiếp tục được củng cố",
                summary = "Các bên nhất trí mở rộng hợp tác về thương mại và hạ tầng kết nối.",
                daysAgo = 1,
                minuteOffset = 5,
            ),
            Draft(
                id = "ct-05",
                title = "Chính sách an sinh giai đoạn mới",
                summary = "Ưu tiên mở rộng diện bao phủ và nâng mức hỗ trợ cho nhóm yếu thế.",
                daysAgo = 1,
                minuteOffset = 35,
            ),
            Draft(
                id = "ct-06",
                title = "Chương trình phát triển hạ tầng giao thông",
                summary = "Danh mục dự án trọng điểm được điều chỉnh để bảo đảm tiến độ giải ngân.",
                daysAgo = 1,
                minuteOffset = 55,
            ),
            Draft(
                id = "ct-07",
                title = "Đối thoại về ứng phó biến đổi khí hậu",
                summary = "Các cam kết giảm phát thải được rà soát lại theo tiến độ thực tế.",
                daysAgo = 2,
                minuteOffset = 12,
            ),
            Draft(
                id = "ct-08",
                title = "Công khai minh bạch ngân sách địa phương",
                summary = "Dữ liệu chi tiêu được công bố theo định kỳ, thuận tiện tra cứu.",
                daysAgo = 2,
                minuteOffset = 31,
            ),
            Draft(
                id = "ct-09",
                title = "Chuyển đổi số trong cơ quan nhà nước",
                summary = "Nhiều dịch vụ công được đưa lên trực tuyến toàn trình.",
                daysAgo = 2,
                minuteOffset = 50,
            ),
        ),
        CATEGORY_TECHNOLOGY to listOf(
            Draft(
                id = "cn-01",
                title = "Trung tâm dữ liệu xanh đi vào hoạt động",
                summary = "Cơ sở mới sử dụng hệ thống làm mát tiết kiệm điện và nguồn năng lượng tái tạo.",
                daysAgo = 0,
                minuteOffset = 12,
            ),
            Draft(
                id = "cn-02",
                title = "Chip xử lý AI tiết kiệm điện hơn",
                summary = "Thế hệ chip mới giảm đáng kể điện năng tiêu thụ cho cùng khối lượng tính toán.",
                daysAgo = 0,
                minuteOffset = 28,
            ),
            Draft(
                id = "cn-03",
                title = "An ninh mạng thành ưu tiên của doanh nghiệp",
                summary = "Nhiều doanh nghiệp tăng ngân sách cho bảo mật sau các sự cố gần đây.",
                daysAgo = 0,
                minuteOffset = 47,
            ),
            Draft(
                id = "cn-04",
                title = "Điện toán lượng tử tiến thêm một bước",
                summary = "Số lượng qubit ổn định tăng lên, mở ra khả năng thử nghiệm bài toán lớn hơn.",
                daysAgo = 1,
                minuteOffset = 18,
            ),
            Draft(
                id = "cn-05",
                title = "Thiết bị đeo theo dõi sức khoẻ phổ biến hơn",
                summary = "Cảm biến mới cho phép theo dõi liên tục nhiều chỉ số sức khoẻ quan trọng.",
                daysAgo = 1,
                minuteOffset = 39,
            ),
            Draft(
                id = "cn-06",
                title = "Mã nguồn mở được ứng dụng rộng rãi",
                summary = "Nhiều tổ chức chuyển sang dùng nền tảng mã nguồn mở để giảm phụ thuộc.",
                daysAgo = 1,
                minuteOffset = 58,
            ),
            Draft(
                id = "cn-07",
                title = "Mạng thế hệ mới bắt đầu được thử nghiệm",
                summary = "Các thử nghiệm đầu tiên tập trung vào độ trễ thấp và mật độ kết nối cao.",
                daysAgo = 2,
                minuteOffset = 20,
            ),
            Draft(
                id = "cn-08",
                title = "Pin thể rắn tiến gần sản xuất đại trà",
                summary = "Công nghệ pin mới hứa hẹn tăng mật độ năng lượng và độ an toàn.",
                daysAgo = 2,
                minuteOffset = 38,
            ),
            Draft(
                id = "cn-09",
                title = "Trợ lý ảo hỗ trợ lập trình viên",
                summary = "Công cụ mới giúp rút ngắn thời gian viết và kiểm thử mã nguồn.",
                daysAgo = 2,
                minuteOffset = 57,
            ),
        ),
    )

    private val articles: List<Article> = draftsByCategory.flatMap { (categoryId, drafts) ->
        drafts.map { draft -> draft.toArticle(categoryId) }
    }

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

    private fun Draft.toArticle(categoryId: String): Article = Article(
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
}
