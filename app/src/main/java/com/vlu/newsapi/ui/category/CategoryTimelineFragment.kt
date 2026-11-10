package com.vlu.newsapi.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.vlu.newsapi.R
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.databinding.FragmentCategoryTimelineBinding
import com.vlu.newsapi.ui.common.returnToMain
import com.vlu.newsapi.ui.detail.ArticleDetailFragment
import kotlinx.coroutines.launch

/**
 * Màn 2 — danh sách bài báo của một chuyên mục, tự chia theo ngày đăng (Mục 2 của đặc tả).
 *
 * Header: [←]  "Chuyên mục: Kinh tế".
 * Thân trang: mốc ngày ở trên, các thẻ nằm ngang ở dưới, không có ảnh nhỏ trong thẻ.
 *
 * Thoát trang theo hai đường như đặc tả yêu cầu: nút [←] hoặc lướt từ trái sang phải (cử chỉ do
 * [com.vlu.newsapi.ui.common.SwipeBackLayout] nhận biết).
 */
class CategoryTimelineFragment : Fragment() {

    private var _binding: FragmentCategoryTimelineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryTimelineViewModel by viewModels {
        CategoryTimelineViewModel.factory(
            requireArguments().getString(ARG_CATEGORY_ID).orEmpty(),
        )
    }

    private val adapter = CategoryTimelineAdapter(onArticleClick = ::openArticleDetail)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoryTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lướt từ trái sang phải để quay về Main. Lớp bọc chỉ nhận biết cử chỉ chứ không tiêu
        // thụ sự kiện, nên bấm nút, bấm thẻ và cuộn danh sách vẫn hoạt động như cũ.
        binding.root.onSwipeRight = { returnToMain() }

        binding.btnBackCategory.setOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.btnCategoryRetry.setOnClickListener { viewModel.retry() }

        binding.rvCategoryTimeline.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryTimeline.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: TimelineUiState) {
        binding.progressCategory.isVisible = state.isLoading

        val showMessage = !state.isLoading && (state.hasError || state.days.isEmpty())
        binding.errorCategory.isVisible = showMessage
        binding.tvCategoryError.setText(
            if (state.hasError) R.string.error_load_news else R.string.empty_articles,
        )
        binding.btnCategoryRetry.isVisible = state.hasError
        binding.rvCategoryTimeline.isVisible =
            !state.isLoading && !state.hasError && state.days.isNotEmpty()

        if (state.categoryName.isNotEmpty()) {
            binding.tvCategoryTitle.text =
                getString(R.string.category_title_format, state.categoryName)
        }

        // Làm phẳng các nhóm ngày thành danh sách dòng để adapter 2 ViewType hiển thị.
        val items = ArrayList<TimelineItem>()
        state.days.forEach { day ->
            items += TimelineItem.DateHeader(day.dayStartMillis)
            day.articles.forEach { article -> items += TimelineItem.ArticleCard(article) }
        }
        adapter.submitList(items)
    }

    private fun openArticleDetail(article: Article) {
        Navigation.findNavController(requireView()).navigate(
            R.id.articleDetailFragment,
            Bundle().apply { putString(ArticleDetailFragment.ARG_ARTICLE_ID, article.id) },
        )
    }

    companion object {
        /** Mã chuyên mục truyền từ màn Main sang, khớp khai báo trong nav_graph. */
        const val ARG_CATEGORY_ID = "categoryId"
    }
}
