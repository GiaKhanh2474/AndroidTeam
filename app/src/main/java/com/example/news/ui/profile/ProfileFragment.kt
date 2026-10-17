package com.example.news.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Màn 5 — tài khoản: avatar, thông tin cá nhân, nâng cấp, cài đặt, biểu đồ đã lưu và
 * bài báo đã lưu.
 *
 * Bước này mới dựng header đúng đặc tả ([←] TÀI KHOẢN CÁ NHÂN [Đăng xuất]) và đường
 * quay về Main.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_back)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.headerToolbar.inflateMenu(R.menu.menu_profile)
        binding.headerToolbar.setOnMenuItemClickListener { item ->
            // Chưa có tài khoản thật nên chưa xử lý đăng xuất.
            if (item.itemId == R.id.action_logout) {
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
}
