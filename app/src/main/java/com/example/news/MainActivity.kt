package com.example.news

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.news.databinding.ActivityMainBinding
import com.example.news.ui.common.applySystemBarAndImeInsets

/**
 * Single Activity của ứng dụng.
 *
 * Activity chỉ giữ NavHost, KHÔNG giữ toolbar: đặc tả yêu cầu header của 5 màn khác
 * nhau hoàn toàn (Main có [☰][🔍][👤], Chuyên mục có [←]...[📅], Thị trường có [☰]...[✕],
 * Tài khoản có [←]...[Đăng xuất]). Nếu nhồi tất cả vào một toolbar ở Activity thì mỗi
 * Fragment phải "điều khiển từ xa" toolbar của Activity, rất rối và rò rỉ trạng thái —
 * đặc biệt là header Main vốn gắn với thao tác cuộn của chính nó.
 *
 * Vì vậy mỗi Fragment tự dựng header trong layout của mình.
 * KHÔNG dùng Bottom Navigation theo đặc tả.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đẩy toàn bộ nội dung xuống dưới thanh trạng thái / tai thỏ.
        // Không có bước này thì header của mọi màn sẽ chìm dưới vùng tai thỏ 142px
        // (vừa bị đồng hồ pin đè lên, vừa không chạm được).
        //
        // Dùng bản có xử lý luôn bàn phím vì màn đăng nhập/đăng ký có ô nhập chữ: từ targetSdk 35,
        // `adjustResize` trong manifest không còn tự thu nhỏ cửa sổ, không xử lý thì bàn phím che
        // mất nút "Đăng nhập". Các màn còn lại không có ô nhập chữ nên không bị ảnh hưởng.
        binding.root.applySystemBarAndImeInsets()
    }
}
