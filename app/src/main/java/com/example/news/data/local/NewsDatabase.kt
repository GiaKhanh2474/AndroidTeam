package com.example.news.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Cơ sở dữ liệu cục bộ của ứng dụng.
 *
 * @param version phiên bản lược đồ. Khi đổi cấu trúc bảng PHẢI tăng số này và viết
 *   migration tương ứng; nhờ bật `exportSchema` nên lược đồ từng phiên bản được lưu trong
 *   thư mục `app/schemas` để đối chiếu và kiểm thử migration.
 */
@Database(
    entities = [
        SavedArticleEntity::class,
        FavouriteChartEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun savedArticleDao(): SavedArticleDao

    abstract fun favouriteChartDao(): FavouriteChartDao

    companion object {
        private const val DATABASE_NAME = "news.db"

        fun create(context: Context): NewsDatabase = Room.databaseBuilder(
            context.applicationContext,
            NewsDatabase::class.java,
            DATABASE_NAME,
        ).build()
    }
}
