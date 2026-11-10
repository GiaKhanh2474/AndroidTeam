package com.vlu.newsapi.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vlu.newsapi.NewsApplication
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/** Ba trạng thái của màn kết quả tìm kiếm (API 4). */
data class SearchUiState(
    val isLoading: Boolean = true,
    val results: List<Article> = emptyList(),
    val hasError: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    repository: NewsRepository,
    private val query: String,
) : ViewModel() {

    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<SearchUiState> = refreshRequests
        .flatMapLatest {
            repository.searchArticles(query)
                .map { results -> SearchUiState(isLoading = false, results = results) }
                .onStart { emit(SearchUiState()) }
                .catch { emit(SearchUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SearchUiState(),
        )

    fun retry() {
        refreshRequests.value += 1
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(query: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                SearchViewModel(
                    repository = application.container.newsRepository,
                    query = query,
                )
            }
        }
    }
}
