package com.example.news

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.news.databinding.ActivityMainBinding

/**
 * Single Activity của ứng dụng. Ở Phase 0 chỉ hiển thị màn hình kiểm tra.
 * NavHostFragment sẽ được thêm vào [activity_main] ở Phase 1.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
