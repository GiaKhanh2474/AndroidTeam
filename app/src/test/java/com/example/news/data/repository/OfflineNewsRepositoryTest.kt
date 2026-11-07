package com.example.news.data.repository

import com.example.news.data.model.Article
import com.example.news.data.model.Category
import com.example.news.data.model.CategoryWithArticles
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineNewsRepositoryTest {
    private val article = Article("saved", "category", "title", "summary", "source", 0L, "https://example.com", content = "Full offline content")

    private fun source(get: suspend (String) -> Article?) = object : NewsRepository {
        override fun observeCategories() = flowOf(emptyList<Category>())
        override fun observeMainFeed() = flowOf(emptyList<CategoryWithArticles>())
        override fun observeArticlesOf(categoryId: String) = flowOf(emptyList<Article>())
        override suspend fun getArticle(articleId: String) = get(articleId)
    }

    @Test fun savedArticleOpensWithoutCallingUnavailableSource() = runBlocking {
        val repository = OfflineNewsRepository(source { error("No network") }) { id ->
            if (id == article.id) article else null
        }
        assertEquals(article, repository.getArticle(article.id))
    }

    @Test fun unsavedArticleUsesSource() = runBlocking {
        val repository = OfflineNewsRepository(source { article }) { null }
        assertEquals(article, repository.getArticle(article.id))
    }

    @Test fun deletedSavedArticleIsNotKeptInMemory() = runBlocking {
        var saved: Article? = article
        val repository = OfflineNewsRepository(source { null }) { saved }
        assertEquals(article, repository.getArticle(article.id))
        saved = null
        assertNull(repository.getArticle(article.id))
    }
}
