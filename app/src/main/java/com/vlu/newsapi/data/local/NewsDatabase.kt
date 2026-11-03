package com.vlu.newsapi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        MainFeedArticleEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun savedArticleDao(): SavedArticleDao

    abstract fun favouriteChartDao(): FavouriteChartDao

    abstract fun mainFeedDao(): MainFeedDao

    companion object {
        private const val DATABASE_NAME = "news.db"

        /**
         * Phiên bản 2 thêm bảng cache cho 9 bài của trang Chính.
         *
         * Bảng mới nên không phải chuyển dữ liệu của hai bảng cũ — chỉ cần tạo đúng cấu trúc.
         * Câu lệnh ở đây phải khớp từng cột với [MainFeedArticleEntity]; lệch một cột là Room
         * báo lỗi ngay lần mở cơ sở dữ liệu đầu tiên.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `main_feed_article` (
                        `id` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `categoryPosition` INTEGER NOT NULL,
                        `slot` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `summary` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `publishedAt` INTEGER NOT NULL,
                        `sourceUrl` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `content` TEXT NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
            }
        }

        fun create(context: Context): NewsDatabase = Room.databaseBuilder(
            context.applicationContext,
            NewsDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(MIGRATION_1_2).build()
    }
}
