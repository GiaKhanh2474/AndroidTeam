package com.example.news.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.data.model.Article
import com.example.news.data.model.CategoryWithArticles
import com.example.news.databinding.ItemArticleGridBinding
import com.example.news.databinding.ItemMainCategoryBinding

/**
 * Danh sách 3 "Grid Tổng" của màn Main.
 *
 * Mỗi Grid Tổng là 1 item chứa hàng 3 "Grid Con" nằm ngang. Ba ô được chia đều chiều
 * rộng bằng layout_weight nên luôn khớp với bản vẽ đặc tả.
 */
class MainCategoryAdapter(
    private val onCategoryClick: (CategoryWithArticles) -> Unit,
    private val onArticleClick: (Article) -> Unit,
) : ListAdapter<CategoryWithArticles, MainCategoryAdapter.CategoryViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder =
        CategoryViewHolder(
            binding = ItemMainCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onCategoryClick = onCategoryClick,
            onArticleClick = onArticleClick,
        )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemMainCategoryBinding,
        private val onCategoryClick: (CategoryWithArticles) -> Unit,
        private val onArticleClick: (Article) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val gapPx: Int =
            binding.root.resources.getDimensionPixelSize(R.dimen.spacing_sm)

        fun bind(item: CategoryWithArticles) {
            binding.tvCategoryName.text = item.category.name
            binding.tvCategoryName.setOnClickListener { onCategoryClick(item) }
            binding.btnViewByDate.setOnClickListener { onCategoryClick(item) }

            // View được tái sử dụng nên phải dựng lại hàng thẻ mỗi lần bind,
            // nếu không sẽ thấy bài của chuyên mục trước còn sót lại.
            binding.containerCards.removeAllViews()

            val inflater = LayoutInflater.from(binding.containerCards.context)
            item.articles.forEachIndexed { index, article ->
                val card = ItemArticleGridBinding.inflate(
                    inflater,
                    binding.containerCards,
                    false,
                )
                card.tvArticleTitle.text = article.title
                card.tvArticleSummary.text = article.summary
                card.root.setOnClickListener { onArticleClick(article) }

                val params = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f,
                )
                if (index != item.articles.lastIndex) {
                    params.marginEnd = gapPx
                }
                card.root.layoutParams = params
                binding.containerCards.addView(card.root)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryWithArticles>() {
            override fun areItemsTheSame(
                oldItem: CategoryWithArticles,
                newItem: CategoryWithArticles,
            ): Boolean = oldItem.category.id == newItem.category.id

            override fun areContentsTheSame(
                oldItem: CategoryWithArticles,
                newItem: CategoryWithArticles,
            ): Boolean = oldItem == newItem
        }
    }
}
