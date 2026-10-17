package com.example.news.data.repository

import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import kotlinx.coroutines.flow.Flow

/**
 * Nguồn dữ liệu thị trường.
 *
 * Giống [NewsRepository], UI và ViewModel chỉ biết interface này; bước này dùng bản minh
 * hoạ ([MockMarketRepository]) và sẽ thay bằng bản Retrofit khi chốt được nguồn dữ liệu.
 */
interface MarketRepository {

    /** Toàn bộ biểu đồ có thể theo dõi. */
    fun observeSeries(): Flow<List<MarketSeries>>

    /** Bài báo bên lề ở cuối trang. */
    fun observeSideArticles(): Flow<List<SideArticle>>

    /**
     * Id các biểu đồ người dùng đã đánh dấu ưu tiên, theo thứ tự ưu tiên.
     *
     * Phần tử ĐẦU TIÊN là biểu đồ đang hiện ở đầu trang. Trả về [Flow] vì sau này danh
     * sách này sẽ được lưu trong cơ sở dữ liệu và có thể thay đổi từ nơi khác.
     */
    fun observeFavouriteIds(): Flow<List<String>>

    /** Đưa một biểu đồ lên vị trí đầu tiên (nhấn ngôi sao theo đặc tả). */
    fun promoteToFollowed(seriesId: String)
}
