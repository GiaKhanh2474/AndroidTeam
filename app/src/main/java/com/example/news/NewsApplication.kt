package com.example.news

import android.app.Application
import com.example.news.di.AppContainer

/**
 * Application của ứng dụng.
 *
 * Dùng Service Locator thủ công ([AppContainer]) thay vì Hilt: dự án chỉ có một ít phụ thuộc,
 * dựng tay như thế này dễ đọc hơn và không phải thêm một trình sinh mã nữa. (Room bắt buộc dùng
 * KSP, nhưng đó là chuyện của Room, không phải lý do để kéo thêm Hilt vào.)
 */
class NewsApplication : Application() {

    /** Nơi giữ các phụ thuộc dùng chung toàn ứng dụng. */
    val container: AppContainer by lazy { AppContainer(applicationContext) }
}
