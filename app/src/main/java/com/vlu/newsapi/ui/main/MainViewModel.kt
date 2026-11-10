package com.vlu.newsapi.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vlu.newsapi.NewsApplication
import com.vlu.newsapi.data.model.CategoryWithArticles
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

/**
 * Trạng thái màn Main.
 *
 * Ba trạng thái theo đặc tả:
 *  - `isLoading = true`: đang gọi API 1.
 *  - `hasError = false` và có dữ liệu: tải thành công.
 *  - `hasError = true`: API lỗi và cache cũng không có bài nào để hiển thị.
 */
data class MainUiState(
    val isLoading: Boolean = true,
    val blocks: List<CategoryWithArticles> = emptyList(),
    val hasError: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(repository: NewsRepository) : ViewModel() {

    /** Mỗi lần người dùng bấm "Thử lại" thì tăng số này để chạy lại API 1. */
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<MainUiState> = refreshRequests
        .flatMapLatest {
            repository.observeMainFeed()
                .map { feed ->
                    if (feed.isEmpty()) {
                        // API trả rỗng và cache cũng rỗng: đây là lỗi, không phải "hết tin".
                        MainUiState(isLoading = false, hasError = true)
                    } else {
                        MainUiState(isLoading = false, blocks = feed)
                    }
                }
                .onStart { emit(MainUiState()) }
                .catch { emit(MainUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = MainUiState(),
        )

    fun retry() {
        refreshRequests.value += 1
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                MainViewModel(application.container.newsRepository)
            }
        }
    }
}
