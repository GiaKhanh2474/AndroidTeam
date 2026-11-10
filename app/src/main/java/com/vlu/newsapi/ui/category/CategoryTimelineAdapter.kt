package com.vlu.newsapi.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vlu.newsapi.R
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.databinding.ItemArticleHorizontalCardBinding
import com.vlu.newsapi.databinding.ItemDateHeaderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Một dòng của danh sách ở trang Chuyên mục.
 *
 * Adapter cần hai loại item khác hẳn nhau (mốc ngày và thẻ bài báo), nên dữ liệu được làm phẳng
 * thành một danh sách các dòng. Nhờ vậy `RecyclerView` chỉ cần một adapter và vẫn tái sử dụng
 * view đúng theo từng loại.
 */
sealed interface TimelineItem {

    /** Mốc ngày, ví dụ "Ngày 22/10/2026". */
    data class DateHeader(val dayStartMillis: Long) : TimelineItem

    /** Một thẻ bài báo nằm ngang bên dưới mốc ngày. */
    data class ArticleCard(val article: Article) : TimelineItem
}

/**
 * Adapter 2 ViewType cho trang Chuyên mục (Mục 2.2 của đặc tả).
 *
 *  - [VIEW_TYPE_DATE_HEADER]: `item_date_header.xml`, chỉ có `tv_date_header`.
 *  - [VIEW_TYPE_ARTICLE_CARD]: `item_article_horizontal_card.xml`, chữ to ở trên, chữ nhỏ ở
 *    dưới, KHÔNG có ảnh nhỏ.
 */
class CategoryTimelineAdapter(
    private val onArticleClick: (Article) -> Unit,
) : ListAdapter<TimelineItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is TimelineItem.DateHeader -> VIEW_TYPE_DATE_HEADER
        is TimelineItem.ArticleCard -> VIEW_TYPE_ARTICLE_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> DateHeaderViewHolder(
                binding = ItemDateHeaderBinding.inflate(inflater, parent, false),
                dayLabelTemplate = parent.context.getString(R.string.timeline_day_label),
                dayPattern = parent.context.getString(R.string.pattern_full_date),
            )

            else -> ArticleCardViewHolder(
                binding = ItemArticleHorizontalCardBinding.inflate(inflater, parent, false),
                onArticleClick = onArticleClick,
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimelineItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is TimelineItem.ArticleCard -> (holder as ArticleCardViewHolder).bind(item.article)
        }
    }

    class DateHeaderViewHolder(
        private val binding: ItemDateHeaderBinding,
        private val dayLabelTemplate: String,
        dayPattern: String,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat(dayPattern, Locale.getDefault())

        fun bind(item: TimelineItem.DateHeader) {
            binding.tvDateHeader.text =
                dayLabelTemplate.format(dateFormat.format(Date(item.dayStartMillis)))
        }
    }

    class ArticleCardViewHolder(
        private val binding: ItemArticleHorizontalCardBinding,
        private val onArticleClick: (Article) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.tvCardBigTitle.text = article.title
            binding.tvCardSmallSummary.text = article.summary
            binding.root.setOnClickListener { onArticleClick(article) }
        }
    }

    private companion object {
        const val VIEW_TYPE_DATE_HEADER = 0
        const val VIEW_TYPE_ARTICLE_CARD = 1

        val DIFF = object : DiffUtil.ItemCallback<TimelineItem>() {

            override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean =
                when {
                    oldItem is TimelineItem.DateHeader && newItem is TimelineItem.DateHeader ->
                        oldItem.dayStartMillis == newItem.dayStartMillis

                    oldItem is TimelineItem.ArticleCard && newItem is TimelineItem.ArticleCard ->
                        oldItem.article.id == newItem.article.id

                    else -> false
                }

            override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean =
                oldItem == newItem
        }
    }
}
