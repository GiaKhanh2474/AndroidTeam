package com.vlu.newsapi.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.vlu.newsapi.R
import com.vlu.newsapi.databinding.FragmentArticleDetailBinding
import com.vlu.newsapi.ui.common.GestureHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn 3 — Chi tiết bài báo (Mục 3 của đặc tả).
 *
 * Là Fragment nằm trong NavHost chứ không phải Activity riêng: nhờ vậy nút [←] và cử chỉ lướt
 * phải đều lùi đúng một bước trong ngăn xếp, và màn này dùng chung một Activity với các màn còn
 * lại — không có Bottom Navigation, cũng không có Activity phụ.
 *
 * Ba cử chỉ được giao cho [GestureHelper]:
 *  1. Chạm đúp -> lưu bài vào Room, hiện Toast "Đã lưu bài báo vào mục Cá nhân!".
 *  2. Đọc sâu rồi lướt ngược lên -> hiện mũi tên mờ ↑, nhấn để về đầu bài.
 *  3. Lướt trái sang phải -> thoát trang.
 */
class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val articleId: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_ARTICLE_ID).orEmpty()
    }

    private val viewModel: DetailViewModel by viewModels { DetailViewModel.factory(articleId) }

    private val dayFormatter: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
        SimpleDateFormat(getString(R.string.pattern_day_month), Locale.getDefault())
    }

    private var gestureHelper: GestureHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackDetail.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

        gestureHelper = GestureHelper(
            scrollDetail = binding.nestedScrollDetail,
            btnFadedUpArrow = binding.btnFadedUpArrow,
            onDoubleTapSaveArticle = ::saveArticleOnDoubleTap,
            onSwipeBack = { Navigation.findNavController(view).popBackStack() },
        ).also { it.attach() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gestureHelper = null
        _binding = null
    }

    private fun render(state: DetailUiState) {
        val article = state.article
        if (article == null) {
            if (!state.isLoading) {
                binding.tvDetailHeadline.text = getString(R.string.detail_article_not_found)
            }
            return
        }

        binding.tvDetailHeadline.text = article.title
        binding.tvDetailSourceDate.text = getString(
            R.string.article_source_date,
            article.source,
            dayFormatter.format(Date(article.publishedAt)),
        )

        // Dòng link luôn hiện đúng câu chữ của đặc tả; bấm vào mở trang báo gốc.
        binding.tvOriginalWebLink.setOnClickListener { openSourceUrl(article.sourceUrl) }

        binding.tvDetailBody.text = article.content

        // Ảnh lớn tải bằng Glide. Dùng ảnh giữ chỗ cho lúc đang tải và khi lỗi (ví dụ mất mạng),
        // nên màn hình không bao giờ bị một khung trống.
        Glide.with(this)
            .load(article.imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .centerCrop()
            .into(binding.ivDetailBanner)
    }

    /**
     * Chạm đúp: lưu bài vào Room rồi báo bằng Toast.
     *
     * Chỉ lưu, không bỏ lưu — người dùng không phải nhớ bài đã lưu hay chưa mới dám chạm. Muốn
     * bỏ lưu thì vào màn Tài khoản, nơi có thao tác rõ ràng cho việc đó.
     */
    private fun saveArticleOnDoubleTap() {
        viewLifecycleOwner.lifecycleScope.launch {
            val saved = viewModel.saveArticleOnDoubleTap()
            if (saved) {
                Toast.makeText(
                    requireContext(),
                    R.string.detail_saved_toast,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    /** Mở trang báo gốc. Liên kết hiện là minh hoạ (example.com) nhưng dùng đúng luồng thật. */
    private fun openSourceUrl(url: String) {
        if (url.isBlank()) {
            showLinkError()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (exception: ActivityNotFoundException) {
            // Máy không có ứng dụng nào mở được liên kết.
            showLinkError()
        }
    }

    private fun showLinkError() {
        Snackbar.make(binding.root, R.string.detail_link_no_app, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        /** Khoá bài báo truyền sang màn này, khớp khai báo trong nav_graph. */
        const val ARG_ARTICLE_ID = "articleId"
    }
}
