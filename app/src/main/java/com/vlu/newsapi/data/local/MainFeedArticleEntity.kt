package com.vlu.newsapi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.Category
import com.vlu.newsapi.data.model.CategoryWithArticles

/**
 * Một bài trong 9 ô của ma trận 3×3 ở trang Chính, giữ lại để đọc được khi mất mạng.
 *
 * Vì sao phải lưu CẢ tên chuyên mục và vị trí ô, không chỉ id bài: đây là bản chụp của màn
 * hình, không phải nguồn tin. Khi không có mạng thì không có cách nào hỏi lại "bài này thuộc
 * chuyên mục nào, đứng ở ô thứ mấy", nên mọi thứ cần để dựng lại đúng ma trận phải nằm sẵn
 * trong bảng.
 *
 * @param categoryPosition thứ tự khối chuyên mục (0 = Kinh tế, 1 = Chính trị, 2 = Công nghệ).
 * @param slot vị trí ô trong khối (0..8), giữ đúng thứ tự bài như lúc lấy về.
 * @param cachedAt thời điểm lấy được bài, để sau này có thể hiện "cập nhật lúc…" nếu cần.
 */
@Entity(tableName = "main_feed_article")
data class MainFeedArticleEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val categoryName: String,
    val categoryPosition: Int,
    val slot: Int,
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: Long,
    val sourceUrl: String,
    val imageUrl: String?,
    val content: String,
    val cachedAt: Long,
)

/** Chuyển bản ghi cache về mô hình bài báo dùng chung cho giao diện. */
fun MainFeedArticleEntity.toArticle(): Article = Article(
    id = id,
    categoryId = categoryId,
    title = title,
    summary = summary,
    source = source,
    publishedAt = publishedAt,
    sourceUrl = sourceUrl,
    imageUrl = imageUrl,
    content = content,
)

/**
 * Dựng lại đúng ma trận 3×3 từ các dòng đã cache.
 *
 * Sắp xếp theo (categoryPosition, slot) chứ không theo id hay thời điểm: người dùng phải thấy
 * lại đúng thứ tự ô như lần cuối còn mạng, không phải một thứ tự khác do khoá chính quyết định.
 */
fun List<MainFeedArticleEntity>.toMainFeed(): List<CategoryWithArticles> =
    groupBy { it.categoryId }
        .values
        .sortedBy { rows -> rows.minOf { it.categoryPosition } }
        .map { rows ->
            val ordered = rows.sortedBy { it.slot }
            CategoryWithArticles(
                category = Category(
                    id = ordered.first().categoryId,
                    name = ordered.first().categoryName,
                ),
                articles = ordered.map { it.toArticle() },
            )
        }
