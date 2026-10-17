package com.example.news

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.news.databinding.ActivityMainBinding
import com.example.news.ui.common.applySystemBarInsets

/**
 * Single Activity của ứng dụng.
 *
 * Chỉ có DUY NHẤT toolbar ở đây, KHÔNG dùng Bottom Navigation theo đặc tả.
 * Mọi màn hình đều là Fragment trong [com.example.news.R.navigation.nav_graph],
 * riêng màn chi tiết bài báo là DetailActivity.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    /** Chỉ mainFragment là top-level nên các màn còn lại sẽ hiện mũi tên Up. */
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đẩy toolbar xuống dưới thanh trạng thái/tai thỏ thay vì để nó chìm bên dưới.
        binding.root.applySystemBarInsets()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Chỉ mainFragment là top-level nên các màn còn lại sẽ hiện mũi tên Up.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.mainFragment))

        // Dùng API Toolbar trực tiếp (KHÔNG dùng setSupportActionBar +
        // setupActionBarWithNavController): biến thể ActionBar của AppCompat cần một
        // MenuItem id=android.R.id.home trong options menu mới kích hoạt được mũi tên Up.
        // Ứng dụng này không có options menu ở toolbar nên cách đó sẽ làm mũi tên Up
        // hiển thị nhưng không phản hồi. setupWithNavController gắn thẳng
        // Toolbar.setNavigationOnClickListener nên hoạt động đúng.
        NavigationUI.setupWithNavController(
            binding.toolbar,
            navController,
            appBarConfiguration,
        )
    }

    /** Mũi tên Up dự phòng cho các đường vào khác (menu/dự phòng). */
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) ||
            super.onSupportNavigateUp()
    }
}
