package com.example.news.data.repository

import com.example.news.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Nguồn dữ liệu hồ sơ người dùng.
 *
 * Trả về [UserProfile]? (có thể null) vì khi nối được đăng nhập thật sẽ có trạng thái chưa
 * đăng nhập. Chuẩn bị sẵn từ bây giờ để giao diện không phải viết lại khi thêm đăng nhập.
 */
interface UserRepository {

    fun observeProfile(): Flow<UserProfile?>
}
