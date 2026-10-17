package com.example.news.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.data.model.Article
import com.example.news.data.model.CategoryWithArticles
import com.example.news.databinding.FragmentMainBinding
import com.example.news.detail.DetailActivity
import com.example.news.ui.timeline.CategoryTimelineFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Màn 1 — Main.
 *
 * Hai trạng thái đầu trang theo đặc tả:
 *  - Trạng thái 1 (mới vào): chỉ có chữ "Main" ở góc trái.
 *  - Trạng thái 2 (lướt nhẹ xuống): chữ "Main" mờ hẳn đi, các Grid nhỏ lại và xích
 *    xuống một chút, đồng thời hiện cụm [☰][🔍 Tìm kiếm][👤].
 *
 * KHÔNG dùng Bottom Navigation theo đặc tả.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private val adapter = MainCategoryAdapter(
        onCategoryClick = ::openCategoryTimeline,
        onArticleClick = ::openArticleDetail,
    )

    private var isHeaderCollapsed = false
    private var pointerDownY = 0f
    private var isPointerDragging = false

    /** Quãng kéo tối thiểu (px) để đổi trạng thái đầu trang. */
    private val dragThresholdPx: Int
        get() = resources.getDimensionPixelSize(R.dimen.main_header_scroll_threshold)

    /** Độ xích xuống của các Grid khi thu gọn đầu trang. */
    private val gridShiftPx: Int
        get() = resources.getDimensionPixelSize(R.dimen.main_grid_collapsed_shift)

    /**
     * Theo dõi thao tác kéo trên danh sách mà KHÔNG chặn sự kiện.
     *
     * Đặc tả mô tả trạng thái 2 kích hoạt bằng "lướt nhẹ màn hình xuống". Với chỉ 3
     * chuyên mục thì nội dung có thể vừa khít màn hình và danh sách không cuộn được,
     * khi đó `onScrolled` sẽ không bao giờ chạy. Vì vậy ở đây quan sát trực tiếp quãng
     * kéo của ngón tay: kéo xuống quá ngưỡng thì hiện thanh công cụ, kéo ngược lên thì
     * trả lại chữ "Main".
     */
    private val dragWatcher = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pointerDownY = e.y
                    isPointerDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val delta = e.y - pointerDownY
                    if (!isPointerDragging && abs(delta) >= dragThresholdPx) {
                        isPointerDragging = true
                        setHeaderCollapsed(collapsed = delta > 0f)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPointerDragging = false
            }
            // Trả về false để RecyclerView vẫn xử lý cuộn bình thường.
            return false
        }
    }

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

        // Khung nhìn mới luôn được dựng ở TRẠNG THÁI 1 (chỉ có chữ "Main"). Nhưng
        // Fragment thì được giữ nguyên khi đi sang màn khác rồi quay lại, nên cờ phải
        // được đặt lại. Nếu không, sau khi đã kéo xuống một lần rồi sang màn khác, lần
        // kéo xuống kế tiếp sẽ bị bỏ qua và thanh công cụ không bao giờ hiện lại.
        isHeaderCollapsed = false

        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter
        binding.rvCategories.setHasFixedSize(true)
        binding.rvCategories.addOnItemTouchListener(dragWatcher)

        binding.btnOpenMarketDrawer.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_mainFragment_to_marketDrawerFragment)
        }

        binding.btnOpenProfile.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_mainFragment_to_profileFragment)
        }

        // Đặc tả: nút tìm kiếm "các trang báo ngoài" chưa có nguồn dữ liệu nên chỉ báo
        // cho người dùng biết, KHÔNG bịa ra kết quả tìm kiếm.
        binding.searchBox.setOnClickListener {
            Snackbar.make(binding.root, R.string.search_unavailable, Snackbar.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> adapter.submitList(state.blocks) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Chuyển đổi giữa trạng thái 1 và trạng thái 2 của đầu trang. */
    private fun setHeaderCollapsed(collapsed: Boolean) {
        if (isHeaderCollapsed == collapsed) return
        isHeaderCollapsed = collapsed

        binding.tvMainTitle.animate().cancel()
        binding.tvMainTitle.animate()
            .alpha(if (collapsed) 0f else 1f)
            .setDuration(HEADER_ANIM_MS)
            .start()

        binding.groupMainActions.animate().cancel()
        if (collapsed) {
            // Hiện rồi mờ dần vào (từ 0), tránh trường hợp hiện đột ngột khi animation
            // trước đó bị huỷ giữa chừng.
            binding.groupMainActions.alpha = 0f
            binding.groupMainActions.visibility = View.VISIBLE
        }
        binding.groupMainActions.animate()
            .alpha(if (collapsed) 1f else 0f)
            .setDuration(HEADER_ANIM_MS)
            .withEndAction {
                if (!collapsed) binding.groupMainActions.visibility = View.INVISIBLE
            }
            .start()

        binding.rvCategories.animate().cancel()
        binding.rvCategories.animate()
            .scaleX(if (collapsed) GRID_COLLAPSED_SCALE else 1f)
            .scaleY(if (collapsed) GRID_COLLAPSED_SCALE else 1f)
            .translationY(if (collapsed) gridShiftPx.toFloat() else 0f)
            .setDuration(HEADER_ANIM_MS)
            .start()
    }

    /** Nhấn vào tên chuyên mục hoặc [Xem theo ngày] đều mở trang danh sách theo ngày. */
    private fun openCategoryTimeline(item: CategoryWithArticles) {
        Navigation.findNavController(requireView()).navigate(
            R.id.action_mainFragment_to_categoryTimelineFragment,
            bundleOf(CategoryTimelineFragment.ARG_CATEGORY_ID to item.category.id),
        )
    }

    private fun openArticleDetail(article: Article) {
        startActivity(DetailActivity.newIntent(requireContext(), article.id))
    }

    private companion object {
        const val HEADER_ANIM_MS = 180L
        const val GRID_COLLAPSED_SCALE = 0.96f
    }
}
