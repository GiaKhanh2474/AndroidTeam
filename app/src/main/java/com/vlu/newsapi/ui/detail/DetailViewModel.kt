package com.vlu.newsapi.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vlu.newsapi.NewsApplication
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.repository.NewsRepository
import com.vlu.newsapi.data.repository.SavedArticleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Trạng thái màn Chi tiết.
 *
 * @param article null khi không tìm thấy bài báo theo id (ví dụ id sai).
 * @param notFound true khi backend trả 404 — khác với lỗi mạng và không nên cho bấm "Thử lại".
 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val article: Article? = null,
    val hasError: Boolean = false,
    val notFound: Boolean = false,
)

/**
 * ViewModel của trang đọc bài.
 *
 * Màn hình chỉ biết hai việc: hiển thị bài, và lưu bài khi chạm đúp. Việc lưu đi qua
 * [SavedArticleRepository] nên dữ liệu nằm thật trong Room, mở lại vẫn còn.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    repository: NewsRepository,
    private val savedArticleRepository: SavedArticleRepository,
    private val articleId: String,
) : ViewModel() {

    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<DetailUiState> = refreshRequests
        .flatMapLatest {
            flow { emit(repository.getArticle(articleId)) }
                .map { article ->
                    if (article == null) {
                        DetailUiState(isLoading = false, notFound = true)
                    } else {
                        DetailUiState(isLoading = false, article = article)
                    }
                }
                .onStart { emit(DetailUiState()) }
                .catch { emit(DetailUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = DetailUiState(),
        )

    fun retry() {
        refreshRequests.value += 1
    }

    /**
     * Chạm đúp: lưu bài đang mở vào bảng `saved_article`.
     *
     * Chỉ thêm, không bỏ lưu (xem [SavedArticleRepository.saveArticleOnDoubleTap]).
     *
     * @return true nếu đã lưu, false nếu chưa có bài để lưu (id sai hoặc dữ liệu chưa về) — khi
     *   đó màn hình không hiện thông báo "đã lưu" để tránh báo sai sự thật.
     */
    suspend fun saveArticleOnDoubleTap(): Boolean {
        val article = uiState.value.article ?: return false
        savedArticleRepository.saveArticleOnDoubleTap(article)
        return true
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(articleId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                DetailViewModel(
                    repository = application.container.newsRepository,
                    savedArticleRepository = application.container.savedArticleRepository,
                    articleId = articleId,
                )
            }
        }
    }
}
