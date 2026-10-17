package com.example.news.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.news.databinding.ActivityDetailBinding
import com.example.news.ui.common.applySystemBarInsets

/**
 * Màn 3 — chi tiết bài báo, tách riêng khỏi NavHost theo đặc tả.
 *
 * Bước này mới nhận [EXTRA_ARTICLE_ID] từ màn Main. Tiêu đề, nguồn báo, link bài gốc,
 * ảnh và nội dung sẽ dựng ở bước kế tiếp; sau đó là nhấn đúp để lưu và mũi tên mờ
 * nhảy về đầu trang.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private var articleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đẩy nội dung xuống dưới thanh trạng thái / tai thỏ.
        binding.root.applySystemBarInsets()

        articleId = intent.getStringExtra(EXTRA_ARTICLE_ID)

        binding.btnBack.setOnClickListener { finish() }
    }

    companion object {
        private const val EXTRA_ARTICLE_ID = "extra_article_id"

        fun newIntent(context: Context, articleId: String): Intent =
            Intent(context, DetailActivity::class.java)
                .putExtra(EXTRA_ARTICLE_ID, articleId)
    }
}
