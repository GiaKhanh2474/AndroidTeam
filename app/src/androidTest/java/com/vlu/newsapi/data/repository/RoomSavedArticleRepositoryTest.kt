package com.vlu.newsapi.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.vlu.newsapi.data.local.NewsDatabase
import com.vlu.newsapi.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomSavedArticleRepositoryTest {
    @Test
    fun savingConcurrentlyKeepsExactlyOneRow() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java).build()
        try {
            val repository = RoomSavedArticleRepository(database)
            val article = Article("id", "category", "title", "summary", "source", 0L, "https://example.com")
            coroutineScope {
                (1..20).map {
                    async(Dispatchers.Default) { repository.saveArticleOnDoubleTap(article) }
                }.awaitAll()
            }
            assertEquals(1, repository.observeSavedCount().first())
            assertEquals(listOf(article), repository.observeSavedArticles().first())
        } finally {
            database.close()
        }
    }

    @Test
    fun savingTheSameArticleTwiceKeepsItSaved() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java).build()
        try {
            val repository = RoomSavedArticleRepository(database)
            val article = Article("id", "category", "title", "summary", "source", 0L, "https://example.com")

            // Chạm đúp lần hai vẫn là LƯU (ghi đè đúng bài đó), không phải bỏ lưu.
            repository.saveArticleOnDoubleTap(article)
            repository.saveArticleOnDoubleTap(article)

            assertEquals(1, repository.observeSavedCount().first())
            assertEquals(listOf(article), repository.observeSavedArticles().first())
        } finally {
            database.close()
        }
    }

    @Test
    fun removeDeletesOnlyTheGivenArticle() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java).build()
        try {
            val repository = RoomSavedArticleRepository(database)
            val first = Article("1", "category", "title", "summary", "source", 0L, "https://example.com/1")
            val second = first.copy(id = "2", sourceUrl = "https://example.com/2")
            repository.saveArticleOnDoubleTap(first)
            repository.saveArticleOnDoubleTap(second)

            repository.remove("1")

            assertEquals(listOf(second), repository.observeSavedArticles().first())
            assertEquals(1, repository.observeSavedCount().first())
        } finally {
            database.close()
        }
    }
}
