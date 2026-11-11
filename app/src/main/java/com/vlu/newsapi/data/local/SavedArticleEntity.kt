package com.vlu.newsapi.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vlu.newsapi.data.model.Article

/**
 * Một bài báo người dùng đã lưu (nhấn đúp ở trang Chi tiết).
 *
 * Lưu ĐỦ các trường mà trang Chi tiết cần hiển thị, không chỉ mỗi id. Lý do: bài báo đã lưu
 * phải mở xem lại được kể cả khi sau này nguồn tin đổi hoặc bài bị gỡ khỏi máy chủ. Nếu chỉ
 * lưu id rồi lúc mở lại mới đi lấy nội dung, người dùng sẽ mất bài đã lưu của mình.
 */
@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey @ColumnInfo(name = "articleId") val id: String,
    val categoryId: String,
    val title: String,
    @ColumnInfo(name = "cardSummary") val summary: String,
    @ColumnInfo(name = "sourceName") val source: String,
    val publishedAt: Long,
    @ColumnInfo(name = "originalUrl") val sourceUrl: String,
    val imageUrl: String?,
    @ColumnInfo(name = "fullContent") val content: String,
    /** Thời điểm lưu, dùng để sắp xếp bài mới lưu lên trước. */
    val savedAt: Long,
    val dateLabel: String = "",
)

/** Chuyển bản ghi trong cơ sở dữ liệu về mô hình dùng chung cho giao diện. */
fun SavedArticleEntity.toArticle(): Article = Article(
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

/** Chuyển bài báo thành bản ghi để lưu. */
fun Article.toSavedEntity(savedAt: Long): SavedArticleEntity = SavedArticleEntity(
    id = id,
    categoryId = categoryId,
    title = title,
    summary = summary,
    source = source,
    publishedAt = publishedAt,
    sourceUrl = sourceUrl,
    imageUrl = imageUrl,
    content = content,
    savedAt = savedAt,
    dateLabel = java.text.SimpleDateFormat("'NGÀY' dd/MM/yyyy", java.util.Locale.ROOT).apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    }.format(java.util.Date(publishedAt)),
)
