package com.example.news.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.news.data.local.NewsDatabase
import com.example.news.data.model.Article
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
    fun concurrentTogglesPreserveParity() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java).build()
        try {
            val repository = RoomSavedArticleRepository(database)
            val article = Article("id", "category", "title", "summary", "source", 0L, "https://example.com")
            coroutineScope {
                (1..20).map { async(Dispatchers.Default) { repository.toggle(article) } }.awaitAll()
            }
            assertEquals(0, repository.observeSavedCount().first())
            repository.toggle(article)
            assertEquals(listOf(article), repository.observeSavedArticles().first())
        } finally {
            database.close()
        }
    }
}
