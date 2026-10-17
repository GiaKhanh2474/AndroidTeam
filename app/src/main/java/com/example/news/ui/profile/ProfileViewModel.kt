package com.example.news.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.news.NewsApplication
import com.example.news.data.model.Article
import com.example.news.data.model.MarketSeries
import com.example.news.data.model.UserProfile
import com.example.news.data.repository.FavouriteChartRepository
import com.example.news.data.repository.MarketRepository
import com.example.news.data.repository.SavedArticleRepository
import com.example.news.data.repository.SessionRepository
import com.example.news.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Trạng thái trang Tài khoản.
 *
 * @param profile hồ sơ người dùng, null khi chưa đăng nhập (hiện chưa xảy ra được vì chưa có
 *   đăng nhập, nhưng giao diện đã xử lý sẵn).
 * @param savedArticles bài đã lưu, mới lưu trước.
 * @param savedCharts biểu đồ đã đánh dấu, theo thứ tự ưu tiên; phần tử đầu là biểu đồ đang hiện
 *   ở đầu màn Thị trường.
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val savedArticles: List<Article> = emptyList(),
    val savedCharts: List<MarketSeries> = emptyList(),
)

/**
 * Trang Tài khoản.
 *
 * Trang này ghép bốn nguồn lại:
 *  - hồ sơ người dùng ([UserRepository], hiện là bản minh hoạ);
 *  - bài đã lưu ([SavedArticleRepository], đã lưu thật trong Room);
 *  - biểu đồ ưu tiên ([FavouriteChartRepository], đã lưu thật trong Room);
 *  - danh mục biểu đồ ([MarketRepository]) — cần để đổi id đã lưu thành đủ tên và số liệu.
 *
 * Nhờ vậy khi người dùng lưu thêm bài ở nơi khác rồi quay lại đây, danh sách tự cập nhật.
 */
class ProfileViewModel(
    userRepository: UserRepository,
    private val savedArticleRepository: SavedArticleRepository,
    private val favouriteChartRepository: FavouriteChartRepository,
    private val sessionRepository: SessionRepository,
    marketRepository: MarketRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.observeProfile(),
        savedArticleRepository.observeSavedArticles(),
        favouriteChartRepository.observeFavouriteIds(),
        marketRepository.observeSeries(),
    ) { profile, savedArticles, favouriteIds, series ->
        ProfileUiState(
            isLoading = false,
            profile = profile,
            savedArticles = savedArticles,
            // Chỉ lấy được những biểu đồ còn có trong danh mục; id lạ (ví dụ biểu đồ đã bị nguồn
            // dữ liệu bỏ) bị bỏ qua thay vì hiện một dòng trống.
            savedCharts = favouriteIds.mapNotNull { id -> series.firstOrNull { it.id == id } },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ProfileUiState(),
    )

    /**
     * Xoá dữ liệu người dùng đã lưu trên máy.
     *
     * Xoá cả bài đã lưu lẫn biểu đồ ưu tiên vì mục này trong Cài đặt ghi là "Xoá dữ liệu đã lưu"
     * và hộp thoại xác nhận đã nói rõ là xoá cả hai.
     */
    suspend fun clearSavedData() {
        savedArticleRepository.clear()
        favouriteChartRepository.clear()
    }

    /**
     * Đăng xuất.
     *
     * Chỉ xoá cờ phiên trên máy, để lần mở sau hiện lại màn đăng nhập. KHÔNG xoá dữ liệu đã lưu:
     * bài báo và biểu đồ ưu tiên nằm trên máy này, không thuộc về tài khoản nào (và cũng chưa có
     * tài khoản thật), nên đăng xuất mà xoá chúng là mất dữ liệu của người dùng.
     *
     * suspend vì việc ghi xuống đĩa được đẩy sang luồng nền; gọi xong mới nên rời màn, để nhỡ người
     * dùng tắt ứng dụng ngay sau khi bấm thì lần mở sau vẫn chắc chắn hiện lại màn đăng nhập.
     */
    suspend fun signOut() {
        sessionRepository.setSignedIn(false)
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                ProfileViewModel(
                    userRepository = application.container.userRepository,
                    savedArticleRepository = application.container.savedArticleRepository,
                    favouriteChartRepository = application.container.favouriteChartRepository,
                    sessionRepository = application.container.sessionRepository,
                    marketRepository = application.container.marketRepository,
                )
            }
        }
    }
}
