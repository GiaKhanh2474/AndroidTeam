package com.example.news.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.data.model.Article
import com.example.news.data.model.MarketSeries
import com.example.news.databinding.ItemProfileAccountBinding
import com.example.news.databinding.ItemProfileEmptyBinding
import com.example.news.databinding.ItemProfileRowBinding
import com.example.news.databinding.ItemProfileSectionBinding
import com.example.news.ui.common.DateText
import com.example.news.ui.common.MarketFormat

/**
 * Danh sách của trang Tài khoản.
 *
 * Dùng chung MỘT layout dòng ([com.example.news.R.layout.item_profile_row]) cho cả dòng hướng
 * dẫn lẫn biểu đồ đã lưu lẫn bài báo đã lưu: ba loại chỉ khác nhau ở chữ và ở việc có biểu tượng
 * đầu dòng hay không, nên tách ba layout riêng sẽ chỉ tạo ra ba chỗ để sửa giống nhau.
 */
class ProfileListAdapter(
    private val onRowClick: (ProfileRow) -> Unit,
    private val onSavedChartClick: (MarketSeries) -> Unit,
    private val onSavedArticleClick: (Article) -> Unit,
) : ListAdapter<ProfileListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ProfileListItem.Account -> TYPE_ACCOUNT
        is ProfileListItem.Section -> TYPE_SECTION
        is ProfileListItem.Row -> TYPE_ROW
        is ProfileListItem.SavedChart -> TYPE_ROW
        is ProfileListItem.SavedArticle -> TYPE_ROW
        is ProfileListItem.Empty -> TYPE_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ACCOUNT -> AccountHolder(ItemProfileAccountBinding.inflate(inflater, parent, false))
            TYPE_SECTION -> SectionHolder(ItemProfileSectionBinding.inflate(inflater, parent, false))
            TYPE_ROW -> RowHolder(ItemProfileRowBinding.inflate(inflater, parent, false))
            TYPE_EMPTY -> EmptyHolder(ItemProfileEmptyBinding.inflate(inflater, parent, false))
            else -> error("Loại dòng không hợp lệ: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ProfileListItem.Account -> (holder as AccountHolder).bind(item)
            is ProfileListItem.Section -> (holder as SectionHolder).bind(item)
            is ProfileListItem.Row -> (holder as RowHolder).bindRow(item)
            is ProfileListItem.SavedChart -> (holder as RowHolder).bindChart(item.series)
            is ProfileListItem.SavedArticle -> (holder as RowHolder).bindArticle(item.article)
            is ProfileListItem.Empty -> (holder as EmptyHolder).bind(item)
        }
    }

    private class AccountHolder(
        private val binding: ItemProfileAccountBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProfileListItem.Account) {
            binding.tvAccountName.text = item.name
            binding.tvAccountEmail.text = item.email
            binding.tvAccountPhone.text = item.phone
            binding.ivAvatar.contentDescription = binding.root.context.getString(
                R.string.profile_avatar_content_desc_of,
                item.name,
            )
        }
    }

    private class SectionHolder(
        private val binding: ItemProfileSectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProfileListItem.Section) {
            binding.tvSectionTitle.setText(item.titleRes)
        }
    }

    private inner class RowHolder(
        private val binding: ItemProfileRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindRow(item: ProfileListItem.Row) {
            // Biểu tượng và mũi tên phải được đặt lại ở MỌI lần gắn: view được dùng lại, nếu chỉ
            // đặt lúc hiện thì dòng sau sẽ thừa hưởng biểu tượng của dòng trước.
            binding.ivRowIcon.isVisible = item.iconRes != null
            item.iconRes?.let(binding.ivRowIcon::setImageResource)
            binding.ivRowChevron.isVisible = item.showChevron

            binding.tvRowTitle.setText(item.titleRes)
            binding.tvRowSummary.setText(item.summaryRes)
            binding.rowCard.setOnClickListener { onRowClick(item.row) }
        }

        fun bindChart(series: MarketSeries) {
            bindAsContentRow(
                iconRes = R.drawable.ic_show_chart,
                title = series.name,
                summary = MarketFormat.meta(binding.root.context, series),
                contentDescription = binding.root.context.getString(
                    R.string.profile_open_saved_chart,
                    series.name,
                ),
            )
            binding.rowCard.setOnClickListener { onSavedChartClick(series) }
        }

        fun bindArticle(article: Article) {
            bindAsContentRow(
                iconRes = R.drawable.ic_article,
                title = article.title,
                summary = binding.root.context.getString(
                    R.string.article_source_date,
                    article.source,
                    DateText.dayMonth(binding.root.context, article.publishedAt),
                ),
                contentDescription = binding.root.context.getString(
                    R.string.profile_open_saved_article,
                    article.title,
                ),
            )
            binding.rowCard.setOnClickListener { onSavedArticleClick(article) }
        }

        /**
         * Dòng biểu đồ/bài báo đã lưu: có mũi tên vì bấm vào là mở sang màn khác.
         *
         * Cố ý cũng có biểu tượng đầu dòng như các dòng hướng dẫn ở trên: nếu dòng có biểu tượng
         * còn dòng không thì chữ của hai loại dòng lệch nhau, nhìn như lỗi canh lề.
         */
        private fun bindAsContentRow(
            @DrawableRes iconRes: Int,
            title: String,
            summary: String,
            contentDescription: String,
        ) {
            binding.ivRowIcon.isVisible = true
            binding.ivRowIcon.setImageResource(iconRes)
            binding.ivRowChevron.isVisible = true
            binding.tvRowTitle.text = title
            binding.tvRowSummary.text = summary
            // Đặt mô tả cho cả dòng để trình đọc màn hình đọc được việc bấm vào sẽ làm gì, thay vì
            // chỉ đọc tiêu đề rời rạc.
            binding.rowCard.contentDescription = contentDescription
        }
    }

    private class EmptyHolder(
        private val binding: ItemProfileEmptyBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProfileListItem.Empty) {
            binding.tvEmptyMessage.setText(item.messageRes)
        }
    }

    private companion object {
        const val TYPE_ACCOUNT = 0
        const val TYPE_SECTION = 1
        const val TYPE_ROW = 2
        const val TYPE_EMPTY = 3

        val DiffCallback = object : DiffUtil.ItemCallback<ProfileListItem>() {

            override fun areItemsTheSame(oldItem: ProfileListItem, newItem: ProfileListItem): Boolean =
                when {
                    oldItem is ProfileListItem.Account && newItem is ProfileListItem.Account -> true
                    oldItem is ProfileListItem.Section && newItem is ProfileListItem.Section ->
                        oldItem.titleRes == newItem.titleRes

                    oldItem is ProfileListItem.Row && newItem is ProfileListItem.Row ->
                        oldItem.row == newItem.row

                    oldItem is ProfileListItem.SavedChart && newItem is ProfileListItem.SavedChart ->
                        oldItem.series.id == newItem.series.id

                    oldItem is ProfileListItem.SavedArticle &&
                        newItem is ProfileListItem.SavedArticle -> oldItem.article.id == newItem.article.id

                    oldItem is ProfileListItem.Empty && newItem is ProfileListItem.Empty ->
                        oldItem.messageRes == newItem.messageRes

                    else -> false
                }

            override fun areContentsTheSame(
                oldItem: ProfileListItem,
                newItem: ProfileListItem,
            ): Boolean = oldItem == newItem
        }
    }
}
