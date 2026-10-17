package com.example.news

import android.app.Application
import com.example.news.di.AppContainer

/**
 * Application của ứng dụng.
 *
 * Dùng Service Locator thủ công ([AppContainer]) thay vì Hilt để giữ dự án đơn giản,
 * dễ đọc và không phát sinh thêm trình sinh mã (KSP/kapt) — vốn đang là rủi ro với
 * AGP 9 dùng Kotlin tích hợp sẵn.
 */
class NewsApplication : Application() {

    /** Nơi giữ các phụ thuộc dùng chung toàn ứng dụng. */
    val container: AppContainer by lazy { AppContainer() }
}
