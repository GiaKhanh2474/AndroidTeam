package com.example.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Một biểu đồ người dùng đã đánh dấu ưu tiên (nhấn ngôi sao ở trang Thị trường).
 *
 * @param position thứ tự ưu tiên, SỐ NHỎ HƠN đứng trước. Biểu đồ có position nhỏ nhất là
 *   biểu đồ đang hiện ở đầu trang.
 *
 * Khi đưa một biểu đồ lên đầu, mã sẽ đặt position bằng (nhỏ nhất hiện có - 1) thay vì đánh
 * số lại toàn bộ danh sách. Nhờ vậy chỉ cần hai câu lệnh cho mỗi lần nhấn ngôi sao, không
 * phải cập nhật N dòng.
 */
@Entity(tableName = "favourite_chart")
data class FavouriteChartEntity(
    @PrimaryKey val seriesId: String,
    val position: Int,
    val savedAt: Long,
)
