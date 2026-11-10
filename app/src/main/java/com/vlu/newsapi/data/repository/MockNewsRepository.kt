package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.mock.MockNews
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.Category
import com.vlu.newsapi.data.model.CategoryWithArticles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Bản MINH HOẠ của [NewsRepository]. Chưa gọi mạng, dữ liệu lấy từ [MockNews].
 *
 * Đây là chỗ duy nhất cần thay khi có API thật.
 */
class MockNewsRepository : NewsRepository {

    override fun observeCategories(): Flow<List<Category>> = flowOf(MockNews.categories)

    override fun observeMainFeed(): Flow<List<CategoryWithArticles>> = flowOf(MockNews.mainFeed())

    override fun observeArticlesOf(categoryId: String): Flow<List<Article>> =
        flowOf(MockNews.articlesOf(categoryId))

    override suspend fun getArticle(articleId: String): Article? = MockNews.articleById(articleId)

    override fun searchArticles(query: String): Flow<List<Article>> {
        val keyword = query.trim()
        if (keyword.isEmpty()) return flowOf(emptyList())

        // Bản minh hoạ tìm trong tiêu đề, tóm tắt và tên nguồn, giống phạm vi tìm kiếm của backend.
        return flowOf(
            MockNews.allArticles().filter { article ->
                article.title.contains(keyword, ignoreCase = true) ||
                    article.summary.contains(keyword, ignoreCase = true) ||
                    article.source.contains(keyword, ignoreCase = true)
            },
        )
    }
}
