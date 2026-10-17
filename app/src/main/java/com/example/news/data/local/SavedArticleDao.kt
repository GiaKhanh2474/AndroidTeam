package com.example.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Truy vấn cho bảng bài báo đã lưu.
 *
 * Trả [Flow] ở các câu đọc để giao diện tự cập nhật khi dữ liệu đổi (ví dụ đang mở trang
 * Tài khoản mà lưu thêm bài ở nơi khác thì danh sách tự thêm dòng mới).
 */
@Dao
interface SavedArticleDao {

    @Query("SELECT * FROM saved_article ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT COUNT(*) FROM saved_article")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedArticleEntity)

    @Query("DELETE FROM saved_article WHERE id = :articleId")
    suspend fun deleteById(articleId: String)

    @Query("SELECT * FROM saved_article WHERE id = :articleId")
    suspend fun findById(articleId: String): SavedArticleEntity?
}
