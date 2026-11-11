package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.local.NewsDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Compatibility boundary: existing Market/Profile interfaces remain unchanged. */
class RoomFavouriteChartRepository(database: NewsDatabase) : FavouriteChartRepository {
    private val dao = database.newsLocalDao()
    override fun observeFavouriteIds(): Flow<List<String>> =
        dao.getSavedChartsForProfile().map { rows -> rows.map { it.symbol } }
    override suspend fun promote(seriesId: String) = dao.promoteChartToTop(seriesId)
    override suspend fun remove(seriesId: String) = dao.removeSavedChart(seriesId)
    override suspend fun clear() = dao.clearSavedCharts()
}
