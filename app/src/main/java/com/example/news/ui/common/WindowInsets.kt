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

/**
 * Như [applySystemBarInsets] nhưng cộng thêm phần bàn phím (IME) vào padding dưới.
 *
 * Vì sao cần hàm riêng: khai báo `android:windowSoftInputMode="adjustResize"` trong manifest KHÔNG
 * còn tự thu nhỏ cửa sổ khi bàn phím hiện ra (chế độ edge-to-edge bắt buộc từ `targetSdk 35`, và
 * ứng dụng phải tự xử lý insets). Trên máy ảo, bàn phím che mất nút "Đăng nhập" của màn đăng nhập.
 *
 * Lấy phần LỚN HƠN giữa thanh điều hướng và bàn phím chứ không cộng cả hai: khi bàn phím hiện ra
 * thì thanh điều hướng đã nằm trong vùng bàn phím, cộng cả hai sẽ thừa một khoảng trống.
 *
 * Hiện chỉ màn đăng nhập và đăng ký có ô nhập chữ, nên trên thực tế hàm này chỉ khác hàm thường ở
 * hai màn đó.
 */
fun View.applySystemBarAndImeInsets() {
    val basePaddingLeft = paddingLeft
    val basePaddingTop = paddingTop
    val basePaddingRight = paddingRight
    val basePaddingBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
        view.updatePadding(
            left = basePaddingLeft + insets.left,
            top = basePaddingTop + insets.top,
            right = basePaddingRight + insets.right,
            bottom = basePaddingBottom + maxOf(insets.bottom, ime.bottom),
        )
        WindowInsetsCompat.CONSUMED
    }
}
