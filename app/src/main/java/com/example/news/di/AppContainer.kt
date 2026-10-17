package com.example.news.di

import android.content.Context
import com.example.news.data.local.NewsDatabase
import com.example.news.data.repository.AuthRepository
import com.example.news.data.repository.FavouriteChartRepository
import com.example.news.data.repository.MarketRepository
import com.example.news.data.repository.MockAuthRepository
import com.example.news.data.repository.MockMarketRepository
import com.example.news.data.repository.MockNewsRepository
import com.example.news.data.repository.MockUserRepository
import com.example.news.data.repository.NewsRepository
import com.example.news.data.repository.PreferencesSessionRepository
import com.example.news.data.repository.RoomFavouriteChartRepository
import com.example.news.data.repository.RoomSavedArticleRepository
import com.example.news.data.repository.SavedArticleRepository
import com.example.news.data.repository.SessionRepository
import com.example.news.data.repository.UserRepository

/**
 * Service Locator thủ công.
 *
 * Chia hai nhóm rõ ràng:
 *  - dữ liệu lấy từ BÊN NGOÀI (tin tức, thị trường): hiện là bản minh hoạ, sẽ thay bằng bản
 *    Retrofit khi chốt được API. Chỉ cần đổi đúng một dòng ở đây, ViewModel/UI giữ nguyên vì
 *    chúng chỉ biết interface.
 *  - dữ liệu NGƯỜI DÙNG TẠO RA (bài đã lưu, biểu đồ ưu tiên): lưu thật trong Room.
 *
 * Mọi thứ giữ ở phạm vi toàn ứng dụng (một instance duy nhất) vì chúng cùng đọc ghi trên một
 * cơ sở dữ liệu; tạo nhiều instance Room cho cùng một tệp là cách chắc chắn để sinh lỗi.
 *
 * Vẫn không dùng Hilt: chỉ có một ít phụ thuộc, dựng tay như thế này dễ đọc hơn và không phải
 * thêm một trình sinh mã nữa. (Room thì bắt buộc phải có KSP, nhưng Hilt là chuyện khác.)
 */
class AppContainer(context: Context) {

    private val database: NewsDatabase by lazy { NewsDatabase.create(context) }

    val newsRepository: NewsRepository by lazy { MockNewsRepository() }

    val marketRepository: MarketRepository by lazy { MockMarketRepository() }

    val userRepository: UserRepository by lazy { MockUserRepository() }

    /**
     * Xác thực người dùng.
     *
     * Hiện là bản minh hoạ: KHÔNG kiểm tra mật khẩu, KHÔNG gọi mạng, KHÔNG lưu phiên. Đổi đúng
     * dòng này sang bản dùng Retrofit khi có API tài khoản thật.
     */
    val authRepository: AuthRepository by lazy { MockAuthRepository() }

    /**
     * Phiên "đăng nhập" giữ trên máy — chỉ là một cờ đúng/sai, không phải phiên thật (xem
     * [SessionRepository]). Dùng để lần mở sau vào thẳng trang chính thay vì hiện lại màn đăng nhập.
     */
    val sessionRepository: SessionRepository by lazy { PreferencesSessionRepository(context) }

    val favouriteChartRepository: FavouriteChartRepository by lazy {
        RoomFavouriteChartRepository(database)
    }

    val savedArticleRepository: SavedArticleRepository by lazy {
        RoomSavedArticleRepository(database)
    }
}
