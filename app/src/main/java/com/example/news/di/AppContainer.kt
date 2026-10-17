package com.example.news.di

import com.example.news.data.repository.MockNewsRepository
import com.example.news.data.repository.NewsRepository

/**
 * Service Locator thủ công.
 *
 * Hiện chỉ cấp [NewsRepository] bản minh hoạ. Khi có API thật, chỉ cần thay dòng dưới
 * bằng bản Retrofit; toàn bộ ViewModel/UI giữ nguyên vì chúng chỉ biết interface
 * [NewsRepository].
 */
class AppContainer {

    val newsRepository: NewsRepository by lazy { MockNewsRepository() }
}
