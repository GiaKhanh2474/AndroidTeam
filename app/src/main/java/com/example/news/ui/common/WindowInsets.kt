package com.example.news.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Cộng thêm phần "insets" của thanh hệ thống vào padding của view.
 *
 * Từ `targetSdk 35`, Android BẮT BUỘC chế độ edge-to-edge: cửa sổ ứng dụng trải dài
 * xuống dưới thanh trạng thái, tai thỏ và thanh điều hướng. Ứng dụng phải tự cộng bù
 * phần insets này.
 *
 * Nếu bỏ qua, trên máy ảo Pixel_10a (tai thỏ cao 142px) toolbar và mũi tên Up sẽ nằm
 * chìm bên dưới thanh trạng thái: vừa bị đồng hồ pin đè lên, vừa không bấm được vì
 * thanh trạng thái nuốt sự kiện chạm.
 *
 * Hàm giữ lại padding gốc khai báo trong XML rồi mới cộng insets, nên có thể gọi an
 * toàn cho view đã có `android:padding`.
 */
fun View.applySystemBarInsets() {
    val basePaddingLeft = paddingLeft
    val basePaddingTop = paddingTop
    val basePaddingRight = paddingRight
    val basePaddingBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        view.updatePadding(
            left = basePaddingLeft + insets.left,
            top = basePaddingTop + insets.top,
            right = basePaddingRight + insets.right,
            bottom = basePaddingBottom + insets.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}
