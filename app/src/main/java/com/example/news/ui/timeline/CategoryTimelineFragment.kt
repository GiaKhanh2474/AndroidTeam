package com.example.news.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news.R
import com.example.news.data.model.Article
import com.example.news.databinding.FragmentCategoryTimelineBinding
import com.example.news.detail.DetailActivity
import com.example.news.ui.common.returnToMain
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Màn 2 — danh sách bài báo của một chuyên mục, tự chia theo ngày đăng.
 *
 * Header đặc tả: [←] TÊN CHUYÊN MỤC [📅 Chọn ngày].
 * Nhấn [←] quay về Main. Nhấn vào một bài báo sẽ mở trang Chi tiết.
 *
 * Đặc tả cũng ghi: vuốt từ trái sang phải để quay về Main. Cử chỉ đó do lớp bọc ngoài cùng của
 * màn ([com.example.news.ui.common.SwipeBackLayout]) nhận biết.
 */
class CategoryTimelineFragment : Fragment() {

    private var _binding: FragmentCategoryTimelineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryTimelineViewModel by viewModels {
        CategoryTimelineViewModel.factory(
            requireArguments().getString(ARG_CATEGORY_ID).orEmpty(),
        )
    }

    private val adapter = TimelineDayAdapter(onArticleClick = ::openArticleDetail)

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

        // Vuốt từ trái sang phải để quay về Main. Lớp bọc này chỉ nhận biết cử chỉ chứ không
        // tiêu thụ sự kiện, nên bấm nút, bấm bài và cuộn danh sách vẫn hoạt động như cũ.
        binding.root.onSwipeRight = { returnToMain() }

        binding.headerToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_back)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.headerToolbar.inflateMenu(R.menu.menu_category_timeline)
        binding.headerToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_pick_date) {
                // Chưa có nguồn dữ liệu nên chưa mở được lịch chọn ngày.
                Snackbar.make(binding.root, R.string.feature_unavailable, Snackbar.LENGTH_SHORT)
                    .show()
                true
            } else {
                false
            }
        }

        binding.rvTimeline.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTimeline.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.categoryName.isNotEmpty()) {
                        binding.headerToolbar.title = state.categoryName
                    }
                    adapter.submitList(state.days)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openArticleDetail(article: Article) {
        startActivity(DetailActivity.newIntent(requireContext(), article.id))
    }

    companion object {
        /** id chuyên mục truyền từ màn Main sang. */
        const val ARG_CATEGORY_ID = "categoryId"
    }
}
