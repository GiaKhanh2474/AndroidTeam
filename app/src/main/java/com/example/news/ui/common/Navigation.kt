package com.example.news.ui.common

import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.news.R

/**
 * Quay về màn Main.
 *
 * Đặc tả ghi thao tác vuốt ngang là "quay về Main", nên hàm này đi thẳng về Main chứ không lùi
 * một bước như nút [←]: người dùng có thể đã đi qua nhiều màn (Tài khoản → biểu đồ đã lưu) và
 * vuốt là để về thẳng trang chính.
 *
 * Nếu vì lý do nào đó Main không còn trong ngăn xếp điều hướng thì lùi một bước, để thao tác
 * không rơi vào chỗ không làm gì.
 */
fun Fragment.returnToMain() {
    val navController = Navigation.findNavController(requireView())
    if (!navController.popBackStack(R.id.mainFragment, inclusive = false)) {
        navController.navigateUp()
    }
}
