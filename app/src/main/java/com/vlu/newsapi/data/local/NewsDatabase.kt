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
        TrackedChartEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsLocalDao(): NewsLocalDao

    abstract fun savedArticleDao(): SavedArticleDao

    abstract fun favouriteChartDao(): FavouriteChartDao

    abstract fun mainFeedDao(): MainFeedDao

    companion object {
        private const val DATABASE_NAME = "news_app_local.db"

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE saved_articles (
                    articleId TEXT NOT NULL PRIMARY KEY, categoryId TEXT NOT NULL,
                    title TEXT NOT NULL, cardSummary TEXT NOT NULL, sourceName TEXT NOT NULL,
                    publishedAt INTEGER NOT NULL, originalUrl TEXT NOT NULL, imageUrl TEXT,
                    fullContent TEXT NOT NULL, savedAt INTEGER NOT NULL, dateLabel TEXT NOT NULL)""")
                db.execSQL("""INSERT INTO saved_articles SELECT id, categoryId, title, summary,
                    source, publishedAt, sourceUrl, imageUrl, content, savedAt,
                    'NGÀY ' || strftime('%d/%m/%Y', publishedAt / 1000, 'unixepoch', '+7 hours')
                    FROM saved_article""")
                db.execSQL("DROP TABLE saved_article")
                db.execSQL("""CREATE TABLE tracked_charts (
                    symbol TEXT NOT NULL PRIMARY KEY, displayName TEXT NOT NULL,
                    categoryType TEXT NOT NULL, currentValue REAL NOT NULL, changePercent REAL NOT NULL,
                    isStarredTop INTEGER NOT NULL, isSavedInProfile INTEGER NOT NULL, updatedOrder INTEGER NOT NULL)""")
                SeedDatabaseCallback.seed(db)
                db.execSQL("""INSERT OR REPLACE INTO tracked_charts
                    SELECT seriesId, seriesId, 'COMMODITY', 0.0, 0.0,
                    CASE WHEN position = (SELECT MIN(position) FROM favourite_chart) THEN 1 ELSE 0 END,
                    1, -position FROM favourite_chart""")
            }
        }

        @Volatile private var instance: NewsDatabase? = null

        fun create(context: Context): NewsDatabase = instance ?: synchronized(this) {
            instance ?: build(context).also { instance = it }
        }

        private fun build(context: Context): NewsDatabase = Room.databaseBuilder(
            context.applicationContext,
            NewsDatabase::class.java,
            // Retain installed users' DB path; never silently abandon existing saved articles.
            if (context.getDatabasePath("news.db").exists()) "news.db" else DATABASE_NAME,
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).addCallback(SeedDatabaseCallback()).build()
    }
}
