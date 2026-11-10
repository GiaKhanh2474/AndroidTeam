package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.mock.MockMarket
import com.vlu.newsapi.data.model.MarketSeries
import com.vlu.newsapi.data.model.SideArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Bản MINH HOẠ của [MarketRepository]: chỉ cung cấp danh mục biểu đồ và bài báo bên lề.
 *
 * Danh sách ưu tiên do người dùng đánh dấu không còn ở đây nữa mà đã chuyển sang
 * [RoomFavouriteChartRepository] để lưu được qua các lần mở lại ứng dụng.
 */
class MockMarketRepository : MarketRepository {

    override fun observeSeries(): Flow<List<MarketSeries>> = flowOf(MockMarket.series)

    override fun observeSideArticles(): Flow<List<SideArticle>> = flowOf(MockMarket.sideArticles)
}
