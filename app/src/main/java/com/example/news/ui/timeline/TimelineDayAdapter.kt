package com.example.news.ui.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.data.model.Article
import com.example.news.databinding.ItemArticleRowBinding
import com.example.news.databinding.ItemTimelineDayBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Danh sách bài báo của trang Chuyên mục, mỗi item là MỘT NGÀY.
 *
 * Mỗi ngày gồm tiêu đề mốc ngày và các bài báo nằm ngang bên dưới. Định dạng ngày lấy từ
 * tài nguyên chuỗi ([R.string.timeline_day_label] / [R.string.timeline_day_pattern]) nên
 * không có chuỗi tiếng Việt nào bị viết cứng trong mã.
 */
class TimelineDayAdapter(
    private val onArticleClick: (Article) -> Unit,
) : ListAdapter<DaySection, TimelineDayAdapter.DayViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder =
        DayViewHolder(
            binding = ItemTimelineDayBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            dayLabelTemplate = parent.context.getString(R.string.timeline_day_label),
            dayLabelPattern = parent.context.getString(R.string.timeline_day_pattern),
            onArticleClick = onArticleClick,
        )

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        private val binding: ItemTimelineDayBinding,
        dayLabelTemplate: String,
        dayLabelPattern: String,
        private val onArticleClick: (Article) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dayLabelTemplate = dayLabelTemplate
        private val dateFormat = SimpleDateFormat(dayLabelPattern, Locale.getDefault())

        fun bind(section: DaySection) {
            binding.tvDayLabel.text =
                dayLabelTemplate.format(dateFormat.format(Date(section.dayStartMillis)))

            // View được tái sử dụng nên phải dựng lại danh sách bài mỗi lần bind.
            binding.containerArticles.removeAllViews()

            val inflater = LayoutInflater.from(binding.containerArticles.context)
            section.articles.forEach { article ->
                val row = ItemArticleRowBinding.inflate(inflater, binding.containerArticles, false)
                row.tvArticleTitle.text = article.title
                row.tvArticleSummary.text = article.summary
                row.root.setOnClickListener { onArticleClick(article) }
                binding.containerArticles.addView(row.root)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DaySection>() {
            override fun areItemsTheSame(oldItem: DaySection, newItem: DaySection): Boolean =
                oldItem.dayStartMillis == newItem.dayStartMillis

            override fun areContentsTheSame(oldItem: DaySection, newItem: DaySection): Boolean =
                oldItem == newItem
        }
    }
}
