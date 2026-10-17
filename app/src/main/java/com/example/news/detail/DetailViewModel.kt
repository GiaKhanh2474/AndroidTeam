package com.example.news.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.news.NewsApplication
import com.example.news.data.model.Article
import com.example.news.data.repository.NewsRepository
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

class DetailViewModel(
    repository: NewsRepository,
    articleId: String,
) : ViewModel() {

    val uiState: StateFlow<DetailUiState> = flow { emit(repository.getArticle(articleId)) }
        .map { article -> DetailUiState(isLoading = false, article = article) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = DetailUiState(),
        )

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(articleId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                DetailViewModel(
                    repository = application.container.newsRepository,
                    articleId = articleId,
                )
            }
        }
    }
}
