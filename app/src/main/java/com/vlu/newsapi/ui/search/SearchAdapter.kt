package com.vlu.newsapi.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.databinding.ItemArticleHorizontalCardBinding

/**
 * Danh sách kết quả của API 4.
 *
 * Dùng lại đúng thẻ ngang của màn Chuyên mục (`item_article_horizontal_card`): chữ to là tiêu đề,
 * chữ nhỏ là tóm tắt, không có ảnh nhỏ — để kết quả tìm kiếm trông cùng một hệ thống với phần còn
 * lại của ứng dụng.
 */
class SearchAdapter(
    private val onArticleClick: (Article) -> Unit,
) : ListAdapter<Article, SearchAdapter.ArticleViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder =
        ArticleViewHolder(
            binding = ItemArticleHorizontalCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onArticleClick = onArticleClick,
        )

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArticleViewHolder(
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
        val DIFF = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem == newItem
        }
    }
}
