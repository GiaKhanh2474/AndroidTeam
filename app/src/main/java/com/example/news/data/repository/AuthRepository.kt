package com.example.news.data.repository

/**
 * Nguồn xác thực người dùng.
 *
 * Hiện chỉ có bản minh hoạ [MockAuthRepository]: chưa nối dịch vụ tài khoản nào. Viết sẵn
 * interface để khi có API thật chỉ cần thêm một bản dùng Retrofit rồi đổi đúng một dòng trong
 * `AppContainer`; ViewModel và giao diện không phải sửa.
 *
 * Giao diện đăng nhập/đăng ký hiện tại KHÔNG xác thực gì cả — đây là chỗ giữ chỗ, không phải
 * bằng chứng ứng dụng có hệ thống tài khoản.
 */
interface AuthRepository {

    /**
     * Đăng nhập.
     *
     * @return true nếu cho vào ứng dụng.
     */
    suspend fun signIn(email: String, password: String): Boolean

    /**
     * Tạo tài khoản.
     *
     * @return true nếu cho vào ứng dụng.
     */
    suspend fun signUp(displayName: String, email: String, password: String): Boolean
}
