package com.example.news.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Giữ cờ phiên trong SharedPreferences.
 *
 * Vì sao SharedPreferences chứ không phải Room: đây là một giá trị đúng/sai, phải đọc ngay lúc mới
 * mở ứng dụng (trước khi vẽ màn đầu tiên) nên cần đọc đồng bộ; Room chỉ đọc được bằng luồng nền.
 *
 * Ghi bằng `commit()` chứ không phải `apply()`: `commit()` chỉ trả về sau khi dữ liệu đã nằm trên
 * đĩa, nên "bấm Đăng xuất rồi tắt ứng dụng ngay" vẫn chắc chắn hiện lại màn đăng nhập ở lần mở
 * sau. Việc ghi được đẩy sang luồng IO để không chặn luồng giao diện.
 */
class PreferencesSessionRepository(context: Context) : SessionRepository {

    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun isSignedIn(): Boolean = preferences.getBoolean(KEY_SIGNED_IN, false)

    override suspend fun setSignedIn(signedIn: Boolean) {
        withContext(Dispatchers.IO) {
            preferences.edit().putBoolean(KEY_SIGNED_IN, signedIn).commit()
        }
    }

    private companion object {
        const val FILE_NAME = "news_session"
        const val KEY_SIGNED_IN = "signed_in"
    }
}
