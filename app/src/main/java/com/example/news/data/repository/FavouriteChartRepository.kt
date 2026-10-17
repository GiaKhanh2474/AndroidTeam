package com.example.news.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Danh sách biểu đồ người dùng đánh dấu ưu tiên.
 *
 * Tách riêng khỏi [MarketRepository] vì đây là dữ liệu NGƯỜI DÙNG TẠO RA và được lưu trên
 * máy, còn [MarketRepository] là dữ liệu thị trường lấy từ nguồn bên ngoài. Hai thứ có vòng
 * đời rất khác nhau: xoá dữ liệu thị trường không được làm mất danh sách ưu tiên của người
 * dùng, và ngược lại.
 */
interface FavouriteChartRepository {

    /**
     * Id các biểu đồ đã đánh dấu, theo thứ tự ưu tiên.
     *
     * Phần tử ĐẦU TIÊN là biểu đồ đang hiện ở đầu trang.
     */
    fun observeFavouriteIds(): Flow<List<String>>

    /** Đưa một biểu đồ lên vị trí đầu tiên. */
    suspend fun promote(seriesId: String)

    /** Bỏ đánh dấu một biểu đồ. */
    suspend fun remove(seriesId: String)
}
