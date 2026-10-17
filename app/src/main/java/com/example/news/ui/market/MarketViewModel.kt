package com.example.news.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.news.NewsApplication
import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import com.example.news.data.repository.MarketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Trạng thái trang Thị trường.
 *
 * @param followed biểu đồ đang hiện đầy đủ ở đầu trang (biểu đồ ưu tiên số 1).
 * @param otherSeries các biểu đồ còn lại, đã xếp theo thứ tự ưu tiên.
 * @param favouriteIds id các biểu đồ đã được đánh dấu, dùng để vẽ ngôi sao đặc hay rỗng.
 */
data class MarketUiState(
    val isLoading: Boolean = true,
    val followed: MarketSeries? = null,
    val otherSeries: List<MarketSeries> = emptyList(),
    val favouriteIds: List<String> = emptyList(),
    val sideArticles: List<SideArticle> = emptyList(),
)

/**
 * Trang Thị trường.
 *
 * Việc xếp thứ tự biểu đồ nằm ở đây chứ không ở Fragment: đặc tả nói nhấn ngôi sao thì
 * biểu đồ "nhảy thẳng lên vị trí đầu tiên", tức là một phép sắp xếp trên dữ liệu, không
 * phải thao tác trên view.
 */
class MarketViewModel(
    private val repository: MarketRepository,
) : ViewModel() {

    val uiState: StateFlow<MarketUiState> = combine(
        repository.observeSeries(),
        repository.observeSideArticles(),
        repository.observeFavouriteIds(),
    ) { series, sideArticles, favouriteIds ->
        // Biểu đồ ưu tiên lên trước theo đúng thứ tự đã đánh dấu, phần còn lại giữ nguyên
        // thứ tự gốc. Id không còn tồn tại trong danh sách thì bị bỏ qua.
        val ordered = favouriteIds.mapNotNull { favouriteId ->
            series.firstOrNull { it.id == favouriteId }
        } + series.filter { it.id !in favouriteIds }

        MarketUiState(
            isLoading = false,
            followed = ordered.firstOrNull(),
            otherSeries = ordered.drop(1),
            favouriteIds = favouriteIds,
            sideArticles = sideArticles,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = MarketUiState(),
    )

    /** Nhấn ngôi sao: đưa biểu đồ lên vị trí đầu tiên. */
    fun markAsFollowed(seriesId: String) {
        repository.promoteToFollowed(seriesId)
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                MarketViewModel(application.container.marketRepository)
            }
        }
    }
}
