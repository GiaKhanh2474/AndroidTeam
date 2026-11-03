package com.vlu.newsapi.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.databinding.ItemMainSubGridBinding

/**
 * Adapter cho MỘT ô Grid tổng của ma trận 3×3 ở trang Chính (Mục 1.2 của đặc tả).
 *
 * Mỗi khối chuyên mục có một adapter riêng và một `RecyclerView` dùng `GridLayoutManager(3)`,
 * nên ba ô Grid con luôn nằm trên đúng một hàng ngang. Adapter chỉ biết hiển thị tóm tắt và
 * báo lại cú chạm — việc mở trang Chi tiết do Fragment quyết định.
 *
 * Dùng `ListAdapter` + `DiffUtil` để khi dữ liệu được cập nhật (ví dụ lấy từ cache offline) chỉ
 * những ô thật sự đổi mới được vẽ lại.
 */
class MainSubGridAdapter(
    private val onArticleClick: (Article) -> Unit,
) : ListAdapter<Article, MainSubGridAdapter.SubGridViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubGridViewHolder =
        SubGridViewHolder(
            binding = ItemMainSubGridBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onArticleClick = onArticleClick,
        )

    override fun onBindViewHolder(holder: SubGridViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubGridViewHolder(
        private val binding: ItemMainSubGridBinding,
        private val onArticleClick: (Article) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            // Đặc tả: ô Grid con chỉ hiện đoạn tóm tắt ngắn, không ảnh, không tiêu đề riêng.
            binding.tvSubGridSummary.text = article.summary
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
