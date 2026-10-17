package com.example.news.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.databinding.FragmentCategoryTimelineBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Màn 2 — danh sách bài báo theo chuyên mục, nhóm theo ngày đăng.
 *
 * Bước này mới dựng header đúng đặc tả ([←] TÊN CHUYÊN MỤC [📅 Chọn ngày]) và đường
 * quay về Main. Danh sách thẻ nằm ngang sẽ dựng ở bước kế tiếp, khi đó header sẽ lấy
 * tên chuyên mục thật từ [ARG_CATEGORY_ID].
 */
class CategoryTimelineFragment : Fragment() {

    private var _binding: FragmentCategoryTimelineBinding? = null
    private val binding get() = _binding!!

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

        binding.headerToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_back)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.headerToolbar.inflateMenu(R.menu.menu_category_timeline)
        binding.headerToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_pick_date) {
                Snackbar.make(binding.root, R.string.feature_unavailable, Snackbar.LENGTH_SHORT)
                    .show()
                true
            } else {
                false
            }
        }

        binding.btnBackToMain.setOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** id chuyên mục truyền từ màn Main sang. */
        const val ARG_CATEGORY_ID = "categoryId"
    }
}
