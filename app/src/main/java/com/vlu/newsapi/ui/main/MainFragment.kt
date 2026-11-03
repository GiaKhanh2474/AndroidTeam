package com.vlu.newsapi.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vlu.newsapi.MainActivity
import com.vlu.newsapi.R
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.CategoryIds
import com.vlu.newsapi.data.model.CategoryWithArticles
import com.vlu.newsapi.databinding.FragmentMainBinding
import com.vlu.newsapi.ui.category.CategoryTimelineFragment
import com.vlu.newsapi.ui.detail.ArticleDetailFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Màn 1 — Main (Mục 1 của đặc tả).
 *
 * Hai trạng thái đầu trang:
 *  - Trạng thái 1 (`scrollY <= 20`): chỉ chữ "Main", thanh công cụ ẩn.
 *  - Trạng thái 2 (`scrollY > 20`): chữ "Main" ẩn, thanh [☰] - [Search] - [👤] hiện, khối
 *    Grid thu nhỏ 0.96 và xích xuống 12px trong 150ms.
 *
 * Ma trận 3×3: ba chuyên mục xếp dọc, mỗi chuyên mục là một `RecyclerView` ba cột
 * ([GridLayoutManager] 3). Mỗi ô chỉ có chữ tóm tắt, không ảnh.
 *
 * KHÔNG dùng Bottom Navigation: điều hướng bằng thanh công cụ và cử chỉ.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private val economyAdapter = MainSubGridAdapter(onArticleClick = ::openArticleDetail)
    private val politicsAdapter = MainSubGridAdapter(onArticleClick = ::openArticleDetail)
    private val technologyAdapter = MainSubGridAdapter(onArticleClick = ::openArticleDetail)

    private var isHeaderCollapsed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khung nhìn mới luôn dựng ở trạng thái 1. Fragment được giữ lại khi đi sang màn khác rồi
        // quay về, nên cờ phải đặt lại, nếu không lần cuộn kế tiếp sẽ bị bỏ qua.
        isHeaderCollapsed = false
        applyHeaderState(collapsed = false)

        bindGrid(binding.rvGridKinhTe, economyAdapter)
        bindGrid(binding.rvGridChinhTri, politicsAdapter)
        bindGrid(binding.rvGridCongNghe, technologyAdapter)

        // Nhấn tiêu đề khối -> mở trang Chuyên mục với đúng mã chuyên mục của đặc tả.
        binding.tvHeaderKinhTe.setOnClickListener { openCategory(CategoryIds.ECONOMY) }
        binding.tvHeaderChinhTri.setOnClickListener { openCategory(CategoryIds.POLITICS) }
        binding.tvHeaderCongNghe.setOnClickListener { openCategory(CategoryIds.TECHNOLOGY) }

        // Nút [☰] mở ngăn kéo Biểu đồ & Báo bên lề do MainActivity điều phối.
        binding.btnMenuDrawer.setOnClickListener {
            (activity as? MainActivity)?.openMarketDrawer()
        }

        // Nút [👤] mở trang Tài khoản qua đúng action của nav_graph.
        binding.btnAvatarProfile.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_main_to_profile)
        }

        // Chưa có nguồn tìm kiếm nên chỉ báo cho người dùng biết, KHÔNG bịa kết quả.
        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Snackbar.make(binding.root, R.string.search_unavailable, Snackbar.LENGTH_SHORT)
                    .show()
                true
            } else {
                false
            }
        }

        // Đường chính của đặc tả: đổi trạng thái theo vị trí cuộn của nested_scroll_main.
        binding.nestedScrollMain.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            setHeaderCollapsed(scrollY > SCROLL_TOP_THRESHOLD_PX)
        }

        // Đường dự phòng: khi nội dung ngắn hơn màn hình thì khung không cuộn được, `scrollY`
        // mãi bằng 0 và trạng thái 2 sẽ không bao giờ hiện. View này báo quãng kéo ngón tay để
        // thao tác "lướt xuống" vẫn có tác dụng như đặc tả mô tả.
        binding.nestedScrollMain.onHeaderDrag = { collapsed -> setHeaderCollapsed(collapsed) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderFeed)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindGrid(recyclerView: RecyclerView, adapter: MainSubGridAdapter) {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), GRID_COLUMNS)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)
    }

    /**
     * Đổ dữ liệu vào đúng khối theo mã chuyên mục, không theo thứ tự của danh sách: nếu nguồn
     * dữ liệu trả về thứ tự khác thì màn hình vẫn ghép đúng.
     */
    private fun renderFeed(state: MainUiState) {
        val blocks: List<CategoryWithArticles> = state.blocks
        economyAdapter.submitList(blockOf(blocks, CategoryIds.ECONOMY))
        politicsAdapter.submitList(blockOf(blocks, CategoryIds.POLITICS))
        technologyAdapter.submitList(blockOf(blocks, CategoryIds.TECHNOLOGY))
    }

    private fun blockOf(blocks: List<CategoryWithArticles>, categoryId: String): List<Article> =
        blocks.firstOrNull { it.category.id == categoryId }?.articles.orEmpty()

    private fun setHeaderCollapsed(collapsed: Boolean) {
        if (isHeaderCollapsed == collapsed) return
        isHeaderCollapsed = collapsed
        applyHeaderState(collapsed)
    }

    /** Áp trạng thái đầu trang: ẩn/hiện đúng view và chạy hiệu ứng 150ms cho khối Grid. */
    private fun applyHeaderState(collapsed: Boolean) {
        binding.nestedScrollMain.syncHeaderState(collapsed)
        binding.tvMainLabel.visibility = if (collapsed) View.GONE else View.VISIBLE
        binding.topSmartBar.visibility = if (collapsed) View.VISIBLE else View.GONE

        binding.containerThreeGrids.animate().cancel()
        binding.containerThreeGrids.animate()
            .scaleX(if (collapsed) GRID_COLLAPSED_SCALE else 1f)
            .scaleY(if (collapsed) GRID_COLLAPSED_SCALE else 1f)
            .translationY(if (collapsed) GRID_COLLAPSED_SHIFT_PX else 0f)
            .setDuration(HEADER_ANIM_MS)
            .start()
    }

    private fun openCategory(categoryId: String) {
        Navigation.findNavController(requireView()).navigate(
            R.id.action_mainFragment_to_categoryTimelineFragment,
            Bundle().apply { putString(CategoryTimelineFragment.ARG_CATEGORY_ID, categoryId) },
        )
    }

    private fun openArticleDetail(article: Article) {
        Navigation.findNavController(requireView()).navigate(
            R.id.articleDetailFragment,
            Bundle().apply { putString(ArticleDetailFragment.ARG_ARTICLE_ID, article.id) },
        )
    }

    private companion object {
        /** Đặc tả: mỗi hàng của ma trận có đúng 3 ô Grid con. */
        const val GRID_COLUMNS = 3

        /** Đặc tả: `scrollY > 20` thì chuyển sang trạng thái 2. */
        const val SCROLL_TOP_THRESHOLD_PX = 20

        const val HEADER_ANIM_MS = 150L

        /** Đặc tả: khối Grid thu nhỏ còn 0.96 và dịch xuống 12px. */
        const val GRID_COLLAPSED_SCALE = 0.96f
        const val GRID_COLLAPSED_SHIFT_PX = 12f
    }
}
