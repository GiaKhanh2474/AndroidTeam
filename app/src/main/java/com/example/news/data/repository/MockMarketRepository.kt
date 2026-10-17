package com.example.news.data.repository

import com.example.news.data.mock.MockMarket
import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Bản MINH HOẠ của [MarketRepository].
 *
 * HẠN CHẾ ĐÃ BIẾT: danh sách ưu tiên chỉ nằm trong bộ nhớ của tiến trình. Thoát hẳn ứng
 * dụng rồi mở lại thì danh sách trở về mặc định. Việc lưu thật cần tới tầng cơ sở dữ liệu
 * (Room) — sẽ làm ở bước riêng. Trong một phiên chạy, nhấn ngôi sao rồi đi sang màn khác
 * quay lại vẫn giữ đúng thứ tự.
 */
class MockMarketRepository : MarketRepository {

    private val favouriteIds = MutableStateFlow(listOf(MockMarket.DEFAULT_FOLLOWED_ID))

    override fun observeSeries(): Flow<List<MarketSeries>> = flowOf(MockMarket.series)

    override fun observeSideArticles(): Flow<List<SideArticle>> = flowOf(MockMarket.sideArticles)

    override fun observeFavouriteIds(): StateFlow<List<String>> = favouriteIds.asStateFlow()

    override fun promoteToFollowed(seriesId: String) {
        // Bỏ qua id lạ để không làm hỏng danh sách ưu tiên.
        if (MockMarket.seriesById(seriesId) == null) return

        val current = favouriteIds.value
        favouriteIds.value = listOf(seriesId) + (current - seriesId)
    }
}
