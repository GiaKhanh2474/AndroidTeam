package com.vlu.newsapi.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Seed identities only: zero means not yet fetched, never a fabricated live quote. */
class SeedDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) = seed(db)

    companion object {
        fun seed(db: SupportSQLiteDatabase) {
            listOf(
                Triple("USD_VND", "USD / VND", "CURRENCY"),
                Triple("RON95", "RON95", "FUEL"),
                Triple("E5_RON92", "E5 RON92", "FUEL"),
                Triple("GOLD_SJC", "Vàng SJC", "COMMODITY"),
                Triple("OIL_BRENT", "Dầu Brent", "COMMODITY"),
            ).forEach { (symbol, name, type) ->
                db.execSQL("INSERT OR IGNORE INTO tracked_charts VALUES (?, ?, ?, 0.0, 0.0, 0, 0, 0)", arrayOf(symbol, name, type))
            }
        }
    }
}
