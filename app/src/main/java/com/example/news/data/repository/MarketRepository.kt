package com.example.news.data.repository

import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import kotlinx.coroutines.flow.Flow

/**
 * Nguồn dữ liệu thị trường.
 *
 * Giống [NewsRepository], UI và ViewModel chỉ biết interface này; bước này dùng bản minh
 * hoạ ([MockMarketRepository]) và sẽ thay bằng bản Retrofit khi chốt được nguồn dữ liệu.
 *
 * Danh sách biểu đồ người dùng đánh dấu ưu tiên KHÔNG nằm ở đây mà ở
 * [FavouriteChartRepository], vì đó là dữ liệu người dùng tạo ra và lưu trên máy, có vòng
 * đời khác hẳn dữ liệu thị trường lấy từ bên ngoài.
 */
interface MarketRepository {

    /** Toàn bộ biểu đồ có thể theo dõi. */
    fun observeSeries(): Flow<List<MarketSeries>>

    /** Bài báo bên lề ở cuối trang. */
    fun observeSideArticles(): Flow<List<SideArticle>>
}
