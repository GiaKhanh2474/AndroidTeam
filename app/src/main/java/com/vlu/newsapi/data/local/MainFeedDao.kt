package com.vlu.newsapi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Truy vấn cho bảng cache 9 bài của trang Chính.
 *
 * Bảng này là BẢN CHỤP của màn hình, không phải dữ liệu người dùng tạo ra, nên mỗi lần lấy
 * được dữ liệu mới thì thay sạch toàn bộ: giữ lại bài cũ đã bị gỡ khỏi trang sẽ khiến màn hình
 * offline hiển thị nội dung không còn tồn tại.
 */
@Dao
interface MainFeedDao {

    @Query("SELECT * FROM main_feed_article ORDER BY categoryPosition ASC, slot ASC")
    fun observeAll(): Flow<List<MainFeedArticleEntity>>

    @Query("SELECT * FROM main_feed_article ORDER BY categoryPosition ASC, slot ASC")
    suspend fun getAll(): List<MainFeedArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MainFeedArticleEntity>)

    @Query("DELETE FROM main_feed_article")
    suspend fun deleteAll()

    /**
     * Thay toàn bộ cache bằng dữ liệu vừa lấy về, trong MỘT giao dịch.
     *
     * Nếu tách thành xoá rồi ghi mà giữa hai bước ứng dụng bị đóng, bảng sẽ trống và lần mở
     * offline kế tiếp không còn gì để hiện. Giao dịch bảo đảm người đọc chỉ thấy bản cũ hoặc
     * bản mới trọn vẹn.
     */
    @Transaction
    suspend fun replaceAll(items: List<MainFeedArticleEntity>) {
        deleteAll()
        insertAll(items)
    }
}
