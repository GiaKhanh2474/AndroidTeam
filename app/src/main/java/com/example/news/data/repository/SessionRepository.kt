package com.example.news.data.repository

/**
 * Phiên đăng nhập đang giữ trên máy.
 *
 * Cố ý KHÔNG phải phiên thật: chỉ là một cờ đúng/sai ghi trong bộ nhớ của ứng dụng, không có mã
 * truy cập (token), không có hạn dùng, không liên hệ với máy chủ nào. Mục đích duy nhất là để lần
 * mở sau không phải hiện lại màn đăng nhập.
 *
 * Khi nối được đăng nhập thật thì chỗ này phải thay bằng nơi lưu mã truy cập, và mã đó phải nằm
 * trong bộ nhớ được mã hoá chứ không phải bộ nhớ thường như hiện nay.
 */
interface SessionRepository {

    /** true nếu máy này đã "đăng nhập" và nên vào thẳng trang chính. */
    fun isSignedIn(): Boolean

    /** Ghi lại trạng thái phiên: gọi khi đăng nhập/đăng ký xong và khi đăng xuất. */
    suspend fun setSignedIn(signedIn: Boolean)
}
