package com.vlu.newsapi.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.vlu.newsapi.data.model.Article
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Một bài báo do backend trả về (snake_case, đúng đặc tả `ArticleDto`).
 *
 * Các trường đều để nullable vì Gson không gọi constructor của Kotlin: nếu backend thiếu trường
 * nào thì giá trị sẽ là null chứ không phải giá trị mặc định. Việc chuyển sang [Article] diễn ra
 * trong [toArticle] và có giá trị thay thế cho từng trường.
 */
data class ArticleDto(
    @SerializedName("article_id") val articleId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("main_grid_summary") val mainGridSummary: String? = null,
    @SerializedName("card_summary") val cardSummary: String? = null,
    @SerializedName("full_content") val fullContent: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("original_url") val originalUrl: String? = null,
    @SerializedName("source_name") val sourceName: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("date_label") val dateLabel: String? = null,
    @SerializedName("ai_score") val aiScore: Int? = null,
    @SerializedName("is_main_featured") val isMainFeatured: Boolean? = null,
) {

    /**
     * Chuyển sang model của ứng dụng.
     *
     * @param fallbackCategoryId dùng cho API 1/2 vì hai API này có thể không lặp lại `category`
     *   trong từng bài; API 3/4 truyền null để lấy đúng `category` backend trả về.
     * @param summaryField chọn bản tóm tắt phù hợp màn hình: lưới 3×3 dùng `main_grid_summary`
     *   (15–20 chữ), thẻ ngang dùng `card_summary` (35–50 chữ).
     */
    fun toArticle(
        fallbackCategoryId: String? = null,
        summaryField: SummaryField = SummaryField.CARD,
    ): Article {
        val resolvedCategory = category?.takeIf { it.isNotBlank() }
            ?: fallbackCategoryId.orEmpty()

        val summary = when (summaryField) {
            SummaryField.MAIN_GRID -> mainGridSummary
            SummaryField.CARD -> cardSummary
        }?.takeIf { it.isNotBlank() }
            ?: cardSummary?.takeIf { it.isNotBlank() }
            ?: mainGridSummary.orEmpty()

        return Article(
            id = articleId?.takeIf { it.isNotBlank() } ?: originalUrl.orEmpty(),
            categoryId = resolvedCategory,
            title = title.orEmpty(),
            summary = summary,
            source = sourceName.orEmpty(),
            publishedAt = parsePublishedAt(publishedAt, dateLabel),
            sourceUrl = originalUrl.orEmpty(),
            imageUrl = imageUrl?.takeIf { it.isNotBlank() },
            content = fullContent.orEmpty(),
        )
    }

    enum class SummaryField { MAIN_GRID, CARD }
}

/** Phản hồi API 1 — ba khối của ma trận 3×3. */
data class MainGridsDto(
    val kinhTe: List<ArticleDto>? = null,
    val chinhTri: List<ArticleDto>? = null,
    val congNghe: List<ArticleDto>? = null,
)

/** Phản hồi API 2 — các bài cùng một mốc ngày. */
data class DateGroupDto(
    @SerializedName("date_group") val dateGroup: String? = null,
    val articles: List<ArticleDto>? = null,
)

/**
 * Đọc `published_at` (ISO-8601 do Mongo trả về) thành epoch milliseconds.
 *
 * Dự phòng theo thứ tự: các dạng ISO thường gặp → `date_label` (dd/MM/yyyy) → thời điểm hiện tại.
 * `SimpleDateFormat` được dùng thay cho `java.time` vì ứng dụng đặt minSdk 24.
 */
internal fun parsePublishedAt(publishedAt: String?, dateLabel: String?): Long {
    if (!publishedAt.isNullOrBlank()) {
        for (pattern in ISO_PATTERNS) {
            val parsed = try {
                isoFormatter(pattern).parse(publishedAt)
            } catch (_: ParseException) {
                null
            } catch (_: IllegalArgumentException) {
                // Một số bản Android cũ không hỗ trợ ký tự bù giờ "XXX"; thử mẫu kế tiếp.
                null
            }
            if (parsed != null) return parsed.time
        }
    }

    if (!dateLabel.isNullOrBlank()) {
        val parsed = try {
            dayFormatter().parse(dateLabel)
        } catch (_: ParseException) {
            null
        }
        if (parsed != null) return parsed.time
    }

    return System.currentTimeMillis()
}

private val ISO_PATTERNS = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
)

private fun isoFormatter(pattern: String): SimpleDateFormat =
    SimpleDateFormat(pattern, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }

private fun dayFormatter(): SimpleDateFormat =
    SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }
