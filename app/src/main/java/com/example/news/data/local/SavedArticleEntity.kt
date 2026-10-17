package com.example.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.news.data.model.Article

/**
 * Một bài báo người dùng đã lưu (nhấn đúp ở trang Chi tiết).
 *
 * Lưu ĐỦ các trường mà trang Chi tiết cần hiển thị, không chỉ mỗi id. Lý do: bài báo đã lưu
 * phải mở xem lại được kể cả khi sau này nguồn tin đổi hoặc bài bị gỡ khỏi máy chủ. Nếu chỉ
 * lưu id rồi lúc mở lại mới đi lấy nội dung, người dùng sẽ mất bài đã lưu của mình.
 */
@Entity(tableName = "saved_article")
data class SavedArticleEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: Long,
    val sourceUrl: String,
    val imageUrl: String?,
    val content: String,
    /** Thời điểm lưu, dùng để sắp xếp bài mới lưu lên trước. */
    val savedAt: Long,
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
)
