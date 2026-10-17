package com.example.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Truy vấn cho bảng biểu đồ đánh dấu ưu tiên. */
@Dao
interface FavouriteChartDao {

    /** Đã sắp theo thứ tự ưu tiên, phần tử đầu là biểu đồ hiện ở đầu trang. */
    @Query("SELECT * FROM favourite_chart ORDER BY position ASC")
    fun observeAll(): Flow<List<FavouriteChartEntity>>

    /**
     * Vị trí nhỏ nhất hiện có, hoặc 0 nếu bảng còn trống.
     *
     * Dùng [COALESCE] để câu truy vấn luôn trả về một số, không bao giờ null; nhờ vậy không
     * phải xử lý trường hợp bảng rỗng ở tầng gọi.
     */
    @Query("SELECT COALESCE(MIN(position), 0) FROM favourite_chart")
    suspend fun minPosition(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteChartEntity)

    @Query("DELETE FROM favourite_chart WHERE seriesId = :seriesId")
    suspend fun deleteById(seriesId: String)
}
