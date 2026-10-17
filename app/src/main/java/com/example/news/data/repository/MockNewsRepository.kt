package com.example.news.data.repository

import com.example.news.data.mock.MockNews
import com.example.news.data.model.Article
import com.example.news.data.model.Category
import com.example.news.data.model.CategoryWithArticles
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
}
