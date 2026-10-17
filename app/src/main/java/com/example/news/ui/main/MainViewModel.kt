package com.example.news.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.news.NewsApplication
import com.example.news.data.model.CategoryWithArticles
import com.example.news.data.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY

/**
 * Trạng thái màn Main.
 *
 * @param isLoading hiện chưa dùng tới vì dữ liệu minh hoạ trả về tức thì, nhưng giữ sẵn
 *   để khi nối API thật có thể hiện vòng xoay mà không phải đổi kiểu dữ liệu.
 */
data class MainUiState(
    val isLoading: Boolean = true,
    val blocks: List<CategoryWithArticles> = emptyList(),
)

class MainViewModel(repository: NewsRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = repository.observeMainFeed()
        .map { feed -> MainUiState(isLoading = false, blocks = feed) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = MainUiState(),
        )

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
