package com.vlu.newsapi

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.vlu.newsapi.databinding.ActivityMainBinding
import com.vlu.newsapi.ui.common.applySystemBarAndImeInsets

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
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đẩy toàn bộ nội dung xuống dưới thanh trạng thái / tai thỏ.
        // Không có bước này thì header của mọi màn sẽ chìm dưới vùng tai thỏ 142px
        // (vừa bị đồng hồ pin đè lên, vừa không chạm được).
        //
        // Dùng bản có xử lý luôn bàn phím vì màn đăng nhập/đăng ký có ô nhập chữ: từ targetSdk 35,
        // `adjustResize` trong manifest không còn tự thu nhỏ cửa sổ, không xử lý thì bàn phím che
        // mất nút "Đăng nhập". Các màn còn lại không có ô nhập chữ nên không bị ảnh hưởng.
        binding.root.applySystemBarAndImeInsets()
        binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val drawerHost = supportFragmentManager.findFragmentById(R.id.drawer_market_fragment_container) as NavHostFragment
        val back = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (!drawerHost.navController.popBackStack()) binding.root.closeDrawer(GravityCompat.START)
            }
        }
        onBackPressedDispatcher.addCallback(this, back)
        drawerHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.mainFragment) binding.root.closeDrawer(GravityCompat.START)
        }
        binding.root.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) { back.isEnabled = true }
            override fun onDrawerClosed(drawerView: View) {
                back.isEnabled = false
                drawerHost.navController.popBackStack(R.id.mainFragment, false)
                binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        })

        // Chọn màn mở đầu: lần đầu mở (hoặc sau khi đăng xuất) là màn đăng nhập, còn khi máy đã
        // "đăng nhập" thì vào thẳng trang chính. Làm ở đây chứ không đi qua màn đăng nhập rồi lùi
        // lại, nên người dùng không thấy màn đăng nhập nhấp nháy.
        //
        // Bỏ qua khi hệ thống dựng lại Activity (xoay màn hình, tiến trình bị thu hồi): lúc đó
        // Navigation đã tự khôi phục back stack, đặt lại đồ thị sẽ xoá mất back stack đó.
        if (savedInstanceState == null) {
            chooseStartDestination()
        }
    }

    /**
     * Mở ngăn kéo "Biểu đồ & danh mục mở rộng" (màn Thị trường).
     *
     * Đặc tả yêu cầu nút [☰] ở màn Main gọi `(activity as? MainActivity)?.openMarketDrawer()`
     * thay vì tự điều hướng, để việc chuyển màn nằm ở một chỗ thay vì rải trong Fragment.
     */
    fun openMarketDrawer() {
        val drawerHost = supportFragmentManager.findFragmentById(R.id.drawer_market_fragment_container)
            as? NavHostFragment ?: return
        if (drawerHost.navController.currentDestination?.id == R.id.mainFragment) {
            drawerHost.navController.navigate(R.id.marketDrawerFragment)
        }
        binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        binding.root.openDrawer(GravityCompat.START)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val drawerHost = supportFragmentManager.findFragmentById(R.id.drawer_market_fragment_container)
            as? NavHostFragment ?: return
        if (drawerHost.navController.currentDestination?.id != R.id.mainFragment) {
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.root.openDrawer(GravityCompat.START)
        }
    }

    /**
     * Đặt màn mở đầu của đồ thị điều hướng theo trạng thái phiên trên máy.
     *
     * nav_graph chỉ khai báo được một màn mở đầu cố định, nên đọc lại đồ thị rồi đổi điểm bắt đầu.
     * Nếu vì lý do nào đó chưa lấy được NavHostFragment thì giữ nguyên mặc định của nav_graph (màn
     * đăng nhập): thà phải đăng nhập thêm một lần còn hơn vào thẳng ứng dụng khi chưa xác định được.
     */
    private fun chooseStartDestination() {
        val signedIn = (application as NewsApplication).container.sessionRepository.isSignedIn()

        // Fragment khai báo trong XML được thêm vào FragmentManager bằng một giao dịch chưa chạy
        // ngay, phải chạy nốt thì mới tìm thấy nó.
        supportFragmentManager.executePendingTransactions()
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return

        val graph = navHost.navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(if (signedIn) R.id.mainFragment else R.id.loginFragment)
        navHost.navController.setGraph(graph, null)
    }
}
