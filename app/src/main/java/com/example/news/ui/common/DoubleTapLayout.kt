package com.example.news.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 * LinearLayout có thêm khả năng nhận biết NHẤN ĐÚP ở bất kỳ đâu trong màn hình.
 *
 * Vì sao phải làm một ViewGroup riêng thay vì gọi `setOnTouchListener` trên view gốc:
 * sự kiện chạm đi từ trên xuống, hễ có view con nào xử lý thì view cha KHÔNG nhận được nữa.
 * Nội dung trang Chi tiết nằm trong ScrollView, mà ScrollView xử lý mọi cú chạm vào vùng nội
 * dung, nên view gốc sẽ không bao giờ thấy thao tác của người dùng. Ghi đè
 * [dispatchTouchEvent] thì mọi sự kiện đi qua đây đều được xem xét trước khi chuyển tiếp.
 *
 * View này KHÔNG tiêu thụ sự kiện: kết quả luôn được chuyển tiếp nguyên vẹn cho lớp trên, nên
 * cuộn, bấm nút và bấm liên kết bên trong vẫn hoạt động y như cũ.
 */
class DoubleTapLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    /** Gọi khi người dùng nhấn đúp. */
    var onDoubleTap: (() -> Unit)? = null

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(event: MotionEvent): Boolean {
                onDoubleTap?.invoke()
                return true
            }
        },
    )

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // Chỉ để nhận biết cử chỉ, không quyết định việc tiêu thụ sự kiện.
        gestureDetector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }
}
