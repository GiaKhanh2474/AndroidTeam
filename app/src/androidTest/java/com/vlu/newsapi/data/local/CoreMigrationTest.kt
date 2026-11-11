package com.vlu.newsapi.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CoreMigrationTest {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NewsDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test fun upgradePreservesSavedContentAndChartOrder() {
        helper.createDatabase("migration-test", 2).apply {
            execSQL("INSERT INTO saved_article VALUES ('saved','KINH_TE','Title','Summary','Source',0,'https://example.com',NULL,'Full offline text',123)")
            execSQL("INSERT INTO favourite_chart VALUES ('USD_VND',-2,1)")
            execSQL("INSERT INTO favourite_chart VALUES ('RON95',-1,2)")
            close()
        }
        helper.runMigrationsAndValidate("migration-test", 3, true, NewsDatabase.MIGRATION_2_3).use { db ->
            db.query("SELECT articleId,fullContent,savedAt,dateLabel FROM saved_articles").use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals("saved", cursor.getString(0))
                assertEquals("Full offline text", cursor.getString(1))
                assertEquals(123L, cursor.getLong(2))
                assertEquals("NGÀY 01/01/1970", cursor.getString(3))
            }
            db.query("SELECT symbol FROM tracked_charts WHERE isStarredTop = 1").use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("USD_VND", cursor.getString(0))
            }
        }
    }
}
