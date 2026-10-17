package com.example.news.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.databinding.FragmentMarketDrawerBinding

/**
 * Màn 4 — MarketDrawerFragment.
 *
 * Phase 1: khung rỗng + nút quay về Main.
 * Phase 2: biểu đồ ưu tiên ở đầu trang + bài báo bên lề, tin vắn, bảng xếp hạng.
 * Phase 7: nút ✕ và vuốt phải để quay về Main.
 * Phase 8: biểu đồ thật + nút ⭐ ưu tiên (lưu bằng Room).
 */
class MarketDrawerFragment : Fragment() {

    private var _binding: FragmentMarketDrawerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMarketDrawerBinding.inflate(inflater, container, false)
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
