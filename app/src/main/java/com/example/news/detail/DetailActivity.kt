package com.example.news.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.news.R
import com.example.news.databinding.ActivityDetailBinding
import com.example.news.ui.common.applySystemBarInsets
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn 3 — chi tiết bài báo, tách riêng khỏi NavHost theo đặc tả.
 *
 * Bước này dựng phần nội dung: tiêu đề, nguồn báo + ngày đăng, link trang báo gốc, khung
 * ảnh giữ chỗ và nội dung bài.
 *
 * Các phần còn lại của đặc tả sẽ làm ở bước riêng vì phụ thuộc vào thứ khác:
 *  - nhấn đúp 2 cái để tự lưu bài: cần tầng lưu trữ (Room) thì lưu mới thật sự tồn tại;
 *  - mũi tên ↑ mờ chỉ hiện khi đã cuộn dài rồi vuốt nhẹ lên: là xử lý cử chỉ;
 *  - vuốt từ trái sang phải để thoát: là xử lý cử chỉ.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val articleId: String by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_ARTICLE_ID).orEmpty()
    }

    private val viewModel: DetailViewModel by viewModels { DetailViewModel.factory(articleId) }

    private val dayFormatter: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
        SimpleDateFormat(getString(R.string.pattern_day_month), Locale.getDefault())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đẩy nội dung xuống dưới thanh trạng thái / tai thỏ.
        binding.root.applySystemBarInsets()

        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: DetailUiState) {
        val article = state.article
        if (article == null) {
            if (!state.isLoading) {
                binding.tvDetailTitle.text = getString(R.string.detail_article_not_found)
            }
            return
        }

        binding.tvDetailTitle.text = article.title
        binding.tvDetailSource.text = getString(
            R.string.detail_source_date,
            article.source,
            dayFormatter.format(Date(article.publishedAt)),
        )
        binding.tvDetailLink.text = getString(R.string.detail_source_link, article.sourceUrl)
        binding.tvDetailLink.setOnClickListener { openSourceUrl(article.sourceUrl) }
        binding.tvDetailContent.text = article.content

        // Ảnh vẫn là khung giữ chỗ vì chưa có nguồn ảnh thật.
        // Khi nối được API, thay chỗ này bằng việc nạp article.imageUrl (ví dụ qua Coil).
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
        private const val EXTRA_ARTICLE_ID = "extra_article_id"

        fun newIntent(context: Context, articleId: String): Intent =
            Intent(context, DetailActivity::class.java)
                .putExtra(EXTRA_ARTICLE_ID, articleId)
    }
}
