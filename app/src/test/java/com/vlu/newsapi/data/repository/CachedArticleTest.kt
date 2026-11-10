package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.local.MainFeedArticleEntity
import com.vlu.newsapi.data.local.MainFeedDao
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.Category
import com.vlu.newsapi.data.model.CategoryWithArticles
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CachedArticleTest {
    private val row = MainFeedArticleEntity("id","KINH_TE","Kinh tế",0,0,"Title","Summary","Source",0,"url",null,"Offline text",0)
    private val dao = object : MainFeedDao {
        override fun observeAll() = flowOf(listOf(row))
        override suspend fun getAll() = listOf(row)
        override suspend fun findById(articleId: String) = row.takeIf { it.id == articleId }
        override suspend fun insertAll(items: List<MainFeedArticleEntity>) = Unit
        override suspend fun deleteAll() = Unit
    }
    private fun source(error: Exception) = object : NewsRepository {
        override fun observeCategories() = flowOf(emptyList<Category>())
        override fun observeMainFeed() = flowOf(emptyList<CategoryWithArticles>())
        override fun observeArticlesOf(categoryId: String) = flowOf(emptyList<Article>())
        override fun searchArticles(query: String) = flowOf(emptyList<Article>())
        override suspend fun getArticle(articleId: String): Article? = throw error
    }

    @Test fun cachedHomeArticleCanOpenWhenApiIsOffline() = runBlocking {
        val repository = CachedNewsRepository(source(java.io.IOException("Offline")), dao)
        assertEquals("Offline text", repository.getArticle("id")?.content)
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsNotConvertedIntoCachedSuccess(): Unit = runBlocking {
        CachedNewsRepository(source(CancellationException()), dao).getArticle("id")
        Unit
    }
}
