package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.local.NewsDatabase
import com.vlu.newsapi.data.local.toArticle
import com.vlu.newsapi.data.local.toSavedEntity
import com.vlu.newsapi.data.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Bản lưu thật của [SavedArticleRepository], dùng Room. */
class RoomSavedArticleRepository(
    private val database: NewsDatabase,
) : SavedArticleRepository {

    private val dao = database.savedArticleDao()

    override fun observeSavedArticles(): Flow<List<Article>> =
        dao.observeAll().map { rows -> rows.map { it.toArticle() } }

    override fun observeSavedCount(): Flow<Int> = dao.observeCount()

    /**
     * Chỉ THÊM bài vào bảng, không đọc trạng thái cũ.
     *
     * Đặc tả gọi đây là `saveArticleOnDoubleTap(article)`: chạm đúp là lưu, chạm đúp lần nữa vẫn
     * là lưu (ghi đè đúng bài đó), không có chuyện chạm lần hai lại xoá bài đi. Nhờ vậy người
     * dùng không cần nhớ bài đã lưu hay chưa mới dám chạm.
     */
    override suspend fun saveArticleOnDoubleTap(article: Article) {
        dao.saveArticleOnDoubleTap(article.toSavedEntity(savedAt = System.currentTimeMillis()))
    }

    override suspend fun remove(articleId: String) {
        dao.deleteById(articleId)
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
