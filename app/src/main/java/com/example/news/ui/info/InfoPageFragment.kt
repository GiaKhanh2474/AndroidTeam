package com.example.news.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.databinding.FragmentInfoPageBinding

/**
 * Màn dùng chung cho các dòng "mở màn khác" ở trang Tài khoản.
 *
 * Nhận khoá trang qua đối số điều hướng rồi tra ra tiêu đề và nội dung (xem [InfoPage]).
 *
 * Dùng chung một màn cho mọi dòng để số màn hình của ứng dụng không tăng theo số dòng, trong
 * khi các dòng này hiện đều chỉ là chỗ đang chờ nội dung thật.
 */
class InfoPageFragment : Fragment() {

    private var _binding: FragmentInfoPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInfoPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_back)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        val page = InfoPage.fromKey(arguments?.getString(ARG_PAGE))
        binding.headerToolbar.setTitle(page.titleRes)
        binding.tvInfoBody.setText(page.bodyRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** Khoá đối số trong nav_graph: mục thông tin cần mở. */
        const val ARG_PAGE = "page"
    }
}
