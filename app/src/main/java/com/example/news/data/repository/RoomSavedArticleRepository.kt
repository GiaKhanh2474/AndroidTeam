package com.example.news.data.repository

import com.example.news.data.local.NewsDatabase
import com.example.news.data.local.toArticle
import com.example.news.data.local.toSavedEntity
import com.example.news.data.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Bản lưu thật của [SavedArticleRepository], dùng Room. */
class RoomSavedArticleRepository(
    private val database: NewsDatabase,
) : SavedArticleRepository {

    private val dao = database.savedArticleDao()

    override fun observeSavedArticles(): Flow<List<Article>> =
        dao.observeAll().map { rows -> rows.map { it.toArticle() } }

    override fun observeIsSaved(articleId: String): Flow<Boolean> = dao.observeIsSaved(articleId)

    override fun observeSavedCount(): Flow<Int> = dao.observeCount()

    /**
     * Đọc trước rồi mới ghi.
     *
     * Cố tình không dùng kết quả của câu INSERT để suy ra trạng thái: như vậy mã phụ thuộc vào
     * chi tiết của từng loại câu lệnh, đọc thẳng ra thì rõ ràng hơn và trạng thái trả về luôn
     * khớp với dữ liệu đang có trong bảng.
     */
    override suspend fun toggle(article: Article): Boolean {
        val wasSaved = dao.findById(article.id) != null
        if (wasSaved) {
            dao.deleteById(article.id)
        } else {
            dao.insert(article.toSavedEntity(savedAt = System.currentTimeMillis()))
        }
        return !wasSaved
    }

    override suspend fun remove(articleId: String) {
        dao.deleteById(articleId)
    }
}
