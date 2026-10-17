package com.example.news.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.news.databinding.ActivityDetailBinding
import com.example.news.ui.common.applySystemBarInsets

/**
 * Màn 3 — chi tiết bài báo (tách riêng khỏi NavHost theo đặc tả).
 *
 * Phase 1: khung rỗng + nút quay về.
 * Phase 2: tiêu đề, nguồn tin, thời gian, link bài gốc, ảnh lớn, nội dung.
 * Phase 4: double-tap để lưu (không có nút Save), mũi tên ↑ mờ khi cuộn ngược lên.
 * Phase 5: nhận articleId qua Intent extras.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đẩy nội dung xuống dưới thanh trạng thái/tai thỏ.
        binding.root.applySystemBarInsets()

        binding.btnBack.setOnClickListener { finish() }
    }
}
