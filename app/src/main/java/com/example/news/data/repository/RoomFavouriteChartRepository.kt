package com.example.news.data.repository

import androidx.room.withTransaction
import com.example.news.data.local.FavouriteChartEntity
import com.example.news.data.local.NewsDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Bản lưu thật của [FavouriteChartRepository], dùng Room.
 *
 * Trước đây danh sách ưu tiên chỉ nằm trong bộ nhớ ([MockMarketRepository]) nên thoát hẳn ứng
 * dụng là mất. Nay nó nằm trong cơ sở dữ liệu nên giữ được qua các lần mở lại.
 */
class RoomFavouriteChartRepository(
    private val database: NewsDatabase,
) : FavouriteChartRepository {

    private val dao = database.favouriteChartDao()

    override fun observeFavouriteIds(): Flow<List<String>> =
        dao.observeAll().map { rows -> rows.map { it.seriesId } }

    /**
     * Đưa biểu đồ lên đầu danh sách.
     *
     * Hai câu lệnh (xoá rồi thêm lại với vị trí nhỏ hơn tất cả) PHẢI nằm trong cùng một
     * transaction: nếu chỉ chạy được câu thứ nhất, người dùng sẽ mất biểu đồ vừa đánh dấu.
     */
    override suspend fun promote(seriesId: String) {
        database.withTransaction {
            dao.deleteById(seriesId)
            dao.insert(
                FavouriteChartEntity(
                    seriesId = seriesId,
                    position = dao.minPosition() - 1,
                    savedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun remove(seriesId: String) {
        dao.deleteById(seriesId)
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
