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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Trạng thái màn Chi tiết.
 *
 * @param article null khi không tìm thấy bài báo theo id (ví dụ id sai).
 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val article: Article? = null,
)

/**
 * ViewModel của trang đọc bài.
 *
 * Màn hình chỉ biết hai việc: hiển thị bài, và lưu bài khi chạm đúp. Việc lưu đi qua
 * [SavedArticleRepository] nên dữ liệu nằm thật trong Room, mở lại vẫn còn.
 */
class DetailViewModel(
    repository: NewsRepository,
    private val savedArticleRepository: SavedArticleRepository,
    private val articleId: String,
) : ViewModel() {

    val uiState: StateFlow<DetailUiState> = flow { emit(repository.getArticle(articleId)) }
        .map { article -> DetailUiState(isLoading = false, article = article) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = DetailUiState(),
        )

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
