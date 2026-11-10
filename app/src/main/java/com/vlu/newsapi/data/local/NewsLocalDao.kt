package com.vlu.newsapi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticleOnDoubleTap(article: SavedArticleEntity)

    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun getAllSavedArticles(): Flow<List<SavedArticleEntity>>

    @Query("DELETE FROM saved_articles WHERE articleId = :articleId")
    suspend fun deleteSavedArticleById(articleId: String)

    @Query("SELECT * FROM tracked_charts ORDER BY isStarredTop DESC, updatedOrder DESC")
    fun getOrderedChartsForDrawer(): Flow<List<TrackedChartEntity>>

    @Query("SELECT * FROM tracked_charts WHERE isSavedInProfile = 1 ORDER BY isStarredTop DESC, updatedOrder DESC")
    fun getSavedChartsForProfile(): Flow<List<TrackedChartEntity>>

    @Query("SELECT * FROM tracked_charts WHERE symbol = :symbol")
    suspend fun findChart(symbol: String): TrackedChartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chart: TrackedChartEntity)

    /** Refresh prices without overwriting the user's locally saved preferences. */
    @Transaction
    suspend fun upsertCharts(charts: List<TrackedChartEntity>) {
        charts.forEach { incoming ->
            val old = findChart(incoming.symbol)
            insertChart(if (old == null) incoming else incoming.copy(
                isStarredTop = old.isStarredTop,
                isSavedInProfile = old.isSavedInProfile,
                updatedOrder = old.updatedOrder,
            ))
        }
    }

    @Query("UPDATE tracked_charts SET isStarredTop = 0")
    suspend fun clearAllTopStars()

    @Query("UPDATE tracked_charts SET isStarredTop = 1, isSavedInProfile = 1, updatedOrder = :order WHERE symbol = :symbol")
    suspend fun setStarredTop(symbol: String, order: Long)

    @Query("SELECT COALESCE(MAX(updatedOrder), 0) FROM tracked_charts")
    suspend fun maxOrder(): Long

    @Transaction
    suspend fun promoteChartToTop(symbol: String) {
        if (findChart(symbol) == null) {
            insertChart(TrackedChartEntity(symbol, symbol, "COMMODITY", 0.0, 0.0))
        }
        clearAllTopStars()
        setStarredTop(symbol, maxOf(System.currentTimeMillis(), maxOrder() + 1))
    }

    @Query("UPDATE tracked_charts SET isSavedInProfile = 0, isStarredTop = 0 WHERE symbol = :symbol")
    suspend fun removeSavedChart(symbol: String)

    @Query("UPDATE tracked_charts SET isSavedInProfile = 0, isStarredTop = 0")
    suspend fun clearSavedCharts()
}
