package com.example.news.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.databinding.FragmentCategoryTimelineBinding

/**
 * Màn 2 — CategoryTimelineFragment.
 *
 * Phase 1: khung rỗng + nút quay về Main.
 * Phase 2: danh sách bài báo nhóm theo ngày đăng (card nằm ngang, không ảnh thu nhỏ).
 * Phase 5: nhận categoryId qua Bundle arguments.
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

        binding.btnBackToMain.setOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
