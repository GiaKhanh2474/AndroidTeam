package com.example.news.data.mock

import com.example.news.data.model.UserProfile

/**
 * Hồ sơ MINH HOẠ cho trang Tài khoản.
 *
 * Chưa có đăng nhập thật nên tên, email và số điện thoại đều là dữ liệu bịa:
 *  - email dùng tên miền example.com (tên miền dành riêng cho ví dụ, không trỏ tới đâu);
 *  - số điện thoại là dãy số 0 rõ ràng không phải số thật.
 *
 * Giao diện có nhãn "Hồ sơ minh hoạ" nên không có chỗ nào ngụ ý đây là tài khoản thật.
 */
object MockUser {

    val profile: UserProfile = UserProfile(
        displayName = "Người dùng minh hoạ",
        email = "nguoidung@example.com",
        phone = "0900 000 000",
    )
}
