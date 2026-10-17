package com.example.news.data.model

/**
 * Hồ sơ người dùng đang đăng nhập.
 *
 * Chưa có hệ thống đăng nhập thật nên hiện đây là dữ liệu MINH HOẠ. Kiểu dữ liệu được tách
 * riêng ngay từ đầu để khi nối được API tài khoản thì chỉ việc đổ dữ liệu thật vào, không
 * phải sửa giao diện.
 */
data class UserProfile(
    val displayName: String,
    val email: String,
    val phone: String,
)
