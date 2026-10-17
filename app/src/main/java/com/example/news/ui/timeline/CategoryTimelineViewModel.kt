package com.example.news.ui.timeline

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/** Một mốc NGÀY cùng các bài báo đăng trong ngày đó. */
data class DaySection(
    val dayStartMillis: Long,
    val articles: List<Article>,
)

data class TimelineUiState(
    val isLoading: Boolean = true,
    val categoryName: String = "",
    val days: List<DaySection> = emptyList(),
)

/**
 * Trạng thái trang Chuyên mục.
 *
 * Đặc tả: hệ thống tải hàng loạt bài báo rồi TỰ CHIA theo ngày đăng, ngày nào ra ngày
 * đó ở phía trên. Việc chia nhóm nằm ở ViewModel để Fragment chỉ còn việc hiển thị.
 */
class CategoryTimelineViewModel(
    repository: NewsRepository,
    categoryId: String,
) : ViewModel() {

    val uiState: StateFlow<TimelineUiState> = combine(
        repository.observeCategories(),
        repository.observeArticlesOf(categoryId),
    ) { categories, articles ->
        TimelineUiState(
            isLoading = false,
            categoryName = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            days = articles.groupIntoDays(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = TimelineUiState(),
    )

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(categoryId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                CategoryTimelineViewModel(
                    repository = application.container.newsRepository,
                    categoryId = categoryId,
                )
            }
        }
    }
}

/**
 * Nhóm bài báo theo NGÀY ĐĂNG, ngày mới nhất lên trước.
 *
 * Dùng [Calendar] chứ không dùng `java.time`: `java.time` chỉ có từ API 26 trong khi
 * dự án đặt minSdk 24, nếu dùng sẽ vỡ trên máy Android 7 mà không có cách nào phát hiện
 * lúc build (trừ khi bật desugaring).
 */
private fun List<Article>.groupIntoDays(): List<DaySection> =
    groupBy { it.publishedAt.startOfDayMillis() }
        .entries
        .sortedByDescending { it.key }
        .map { (dayStart, articlesOfDay) ->
            DaySection(
                dayStartMillis = dayStart,
                articles = articlesOfDay.sortedByDescending { it.publishedAt },
            )
        }

private fun Long.startOfDayMillis(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
