package com.example.news.ui.market

import com.example.news.data.model.MarketPoint
import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import com.example.news.data.repository.FavouriteChartRepository
import com.example.news.data.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarketViewModelTest {
    @Test
    fun selectingAnotherChartOverridesInitialProfileFocus() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val a = MarketSeries("a", "A", "unit", 0, listOf(MarketPoint("today", 1f)))
            val b = a.copy(id = "b", name = "B")
            val market = object : MarketRepository {
                override fun observeSeries() = flowOf(listOf(a, b))
                override fun observeSideArticles() = flowOf(emptyList<SideArticle>())
            }
            val favourites = object : FavouriteChartRepository {
                val ids = MutableStateFlow(listOf("b", "a"))
                override fun observeFavouriteIds() = ids
                override suspend fun promote(seriesId: String) {
                    ids.value = listOf(seriesId) + ids.value.filter { it != seriesId }
                }
                override suspend fun remove(seriesId: String) { ids.value -= seriesId }
                override suspend fun clear() { ids.value = emptyList() }
            }
            val model = MarketViewModel(market, favourites, focusedSeriesId = "a")
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.uiState.collect {}
            }
            runCurrent()
            assertEquals("a", model.uiState.value.followed?.id)
            // B is already first in persisted priority, so promote need not emit a new list.
            model.markAsFollowed("b")
            runCurrent()
            assertEquals("b", model.uiState.value.followed?.id)
            assertEquals(listOf("b", "a"), favourites.ids.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
