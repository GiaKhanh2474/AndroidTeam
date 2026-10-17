package com.example.news.data.repository

/**
 * Bản xác thực minh hoạ.
 *
 * Cố ý KHÔNG kiểm tra gì ngoài việc các ô đã có chữ:
 *  - không so mật khẩu với bất cứ đâu,
 *  - không gọi mạng, không đọc/ghi tài khoản,
 *  - không lưu phiên đăng nhập (mở lại ứng dụng vẫn thấy màn đăng nhập).
 *
 * Điều kiện duy nhất ở đây là điều kiện mà giao diện cũng đã kiểm tra trước khi gọi; giữ lại để
 * lớp dữ liệu không bao giờ trả về "thành công" cho một yêu cầu rỗng.
 *
 * Toàn bộ việc kiểm tra thật (gọi API, so mật khẩu, lưu token) sẽ nằm ở đây khi nối nguồn thật.
 */
class MockAuthRepository : AuthRepository {

    override suspend fun signIn(email: String, password: String): Boolean =
        email.isNotBlank() && password.isNotBlank()

    override suspend fun signUp(displayName: String, email: String, password: String): Boolean =
        displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
}
