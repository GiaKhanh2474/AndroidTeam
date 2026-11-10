package com.vlu.newsapi.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.vlu.newsapi.data.local.NewsDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Kiểm tra trên Room thật (in-memory, không dùng mạng): nhấn ⭐ đưa biểu đồ lên đầu và thứ tự
 * đó đọc lại đúng từ cơ sở dữ liệu.
 */
class RoomFavouriteChartRepositoryTest {

    @Test
    fun promoteMovesChartToFrontEachTime() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java).build()
        try {
            val repository = RoomFavouriteChartRepository(database)

            repository.promote("a")
            repository.promote("b")
            repository.promote("c")
            assertEquals(listOf("c", "b", "a"), repository.observeFavouriteIds().first())

            // Đưa biểu đồ đang ở cuối lên đầu; không tạo bản trùng.
            repository.promote("a")
            assertEquals(listOf("a", "c", "b"), repository.observeFavouriteIds().first())

            // Nhấn lại biểu đồ đang ở đầu thì thứ tự giữ nguyên.
            repository.promote("a")
            assertEquals(listOf("a", "c", "b"), repository.observeFavouriteIds().first())
        } finally {
            database.close()
        }
    }
}
