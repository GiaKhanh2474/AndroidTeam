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
import com.example.news.data.repository.FavouriteChartRepository
import com.example.news.data.repository.MarketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
 *
 * Danh mục biểu đồ lấy từ [MarketRepository] (sau này là nguồn dữ liệu thị trường thật), còn
 * danh sách ưu tiên lấy từ [FavouriteChartRepository] (đã lưu trong Room). Hai nguồn được ghép
 * lại ở đây.
 */
class MarketViewModel(
    private val marketRepository: MarketRepository,
    private val favouriteChartRepository: FavouriteChartRepository,
    focusedSeriesId: String? = null,
) : ViewModel() {

    private val focusedSeries = MutableStateFlow(focusedSeriesId)

    val uiState: StateFlow<MarketUiState> = combine(
        marketRepository.observeSeries(),
        marketRepository.observeSideArticles(),
        favouriteChartRepository.observeFavouriteIds(),
        focusedSeries,
    ) { series, sideArticles, favouriteIds, focusedId ->
        val ordered = orderedSeries(series, favouriteIds, focusedId)

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

    /**
     * Xếp thứ tự biểu đồ: ưu tiên đã lưu lên trước theo đúng thứ tự đánh dấu, phần còn lại giữ
     * nguyên thứ tự gốc. Id không còn tồn tại trong danh mục thì bị bỏ qua.
     *
     * Sau đó, nếu màn này được mở từ danh sách "biểu đồ đã lưu" ở trang Tài khoản, biểu đồ được
     * chọn đưa lên đầu. Đây chỉ là CÁCH XEM cho lần mở này: không ghi gì vào cơ sở dữ liệu, nên
     * thứ tự ưu tiên của người dùng không bị đổi chỉ vì họ bấm xem một biểu đồ.
     */
    private fun orderedSeries(
        series: List<MarketSeries>,
        favouriteIds: List<String>,
        focusedId: String?,
    ): List<MarketSeries> {
        val prioritised = favouriteIds.mapNotNull { favouriteId ->
            series.firstOrNull { it.id == favouriteId }
        }
        val inPriorityOrder = prioritised + series.filter { it.id !in favouriteIds }

        val focused = focusedId?.let { id -> inPriorityOrder.firstOrNull { it.id == id } }
            ?: return inPriorityOrder

        return listOf(focused) + inPriorityOrder.filter { it.id != focused.id }
    }

    /** Nhấn ngôi sao: đưa biểu đồ lên vị trí đầu tiên và ghi lại vào cơ sở dữ liệu. */
    fun markAsFollowed(seriesId: String) {
        viewModelScope.launch {
            favouriteChartRepository.promote(seriesId)
            focusedSeries.value = null
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        /**
         * @param focusedSeriesId biểu đồ cần đưa lên đầu trang, hoặc null nếu mở màn theo cách
         *   thường (khi đó dùng đúng thứ tự ưu tiên đã lưu).
         */
        fun factory(focusedSeriesId: String? = null): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                MarketViewModel(
                    marketRepository = application.container.marketRepository,
                    favouriteChartRepository = application.container.favouriteChartRepository,
                    focusedSeriesId = focusedSeriesId,
                )
            }
        }
    }
}
