package com.vlu.newsapi.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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

    // Migrate the previous local flag once, without changing the auth module's interface.
    private val preferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        "news_session_secure",
        MasterKey.Builder(context.applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    ).also { encrypted ->
        val legacy = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        if (legacy.contains(KEY_SIGNED_IN)) {
            if (encrypted.contains(KEY_SIGNED_IN) || encrypted.edit()
                    .putBoolean(KEY_SIGNED_IN, legacy.getBoolean(KEY_SIGNED_IN, false)).commit()) {
                legacy.edit().remove(KEY_SIGNED_IN).commit()
            }
        }
    }

    override fun isSignedIn(): Boolean = preferences.getBoolean(KEY_SIGNED_IN, false)

    override suspend fun setSignedIn(signedIn: Boolean) {
        withContext(Dispatchers.IO) {
            check(preferences.edit().putBoolean(KEY_SIGNED_IN, signedIn).commit()) { "Cannot persist session" }
        }
    }

    private companion object {
        const val FILE_NAME = "news_session"
        const val KEY_SIGNED_IN = "signed_in"
    }
}
