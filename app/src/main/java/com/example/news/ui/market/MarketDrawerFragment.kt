package com.example.news.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.databinding.FragmentMarketDrawerBinding

/**
 * Màn 4 — bảng thị trường (biểu đồ, tin vắn, bảng xếp hạng).
 *
 * Bước này mới dựng header đúng đặc tả ([☰] BIỂU ĐỒ & DANH MỤC MỞ RỘNG [✕]) và nút
 * đóng. Biểu đồ và danh sách biểu đồ sẽ dựng ở các bước sau.
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

        // Đặc tả: biểu tượng bên trái của màn này là 3 gạch, không phải mũi tên quay lại.
        binding.headerToolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_open_market)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.headerToolbar.inflateMenu(R.menu.menu_market_drawer)
        binding.headerToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_close) {
                Navigation.findNavController(view).navigateUp()
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
}
