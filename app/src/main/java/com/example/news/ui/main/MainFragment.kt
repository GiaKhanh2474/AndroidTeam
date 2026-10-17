package com.example.news.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.databinding.FragmentMainBinding
import com.example.news.detail.DetailActivity

/**
 * Màn 1 — Main.
 *
 * Phase 1 mới chỉ là khung điều hướng: các nút ở đây là TẠM THỜI để kiểm chứng
 * Navigation Component. Phase 2 sẽ thay bằng 3 chuyên mục (Kinh tế / Chính trị /
 * Công nghệ), mỗi chuyên mục 3 thẻ bài báo.
 *
 * Lưu ý: KHÔNG có Bottom Navigation theo đặc tả.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

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

        binding.btnCategoryTimeline.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_mainFragment_to_categoryTimelineFragment)
        }

        binding.btnMarketDrawer.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_mainFragment_to_marketDrawerFragment)
        }

        binding.btnProfile.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_mainFragment_to_profileFragment)
        }

        binding.btnDetail.setOnClickListener {
            startActivity(Intent(requireContext(), DetailActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
