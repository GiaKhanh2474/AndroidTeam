package com.example.news.di

import com.example.news.data.repository.MarketRepository
import com.example.news.data.repository.MockMarketRepository
import com.example.news.data.repository.MockNewsRepository
import com.example.news.data.repository.NewsRepository

/**
 * Service Locator thủ công.
 *
 * Hiện chỉ cấp các repository bản minh hoạ. Khi có API thật, chỉ cần thay dòng dưới bằng
 * bản Retrofit; toàn bộ ViewModel/UI giữ nguyên vì chúng chỉ biết interface tương ứng.
 *
 * Repository được giữ ở phạm vi toàn ứng dụng (một instance duy nhất), nhờ vậy trạng thái
 * như danh sách biểu đồ ưu tiên không mất khi người dùng đi sang màn khác rồi quay lại.
 */
class AppContainer {

    val newsRepository: NewsRepository by lazy { MockNewsRepository() }

    val marketRepository: MarketRepository by lazy { MockMarketRepository() }
}
