package com.example.news.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import com.example.news.R
import kotlin.math.abs

/**
 * LinearLayout có thêm khả năng nhận biết thao tác VUỐT NGANG từ trái sang phải.
 *
 * Vì sao phải làm một ViewGroup riêng thay vì gọi `setOnTouchListener` trên view gốc: sự kiện
 * chạm đi từ trên xuống, hễ có view con nào xử lý thì view cha KHÔNG nhận được nữa. Nội dung các
 * màn này nằm trong RecyclerView hoặc ScrollView, mà những view đó xử lý mọi cú chạm vào vùng nội
 * dung, nên view gốc sẽ không bao giờ thấy thao tác của người dùng. Ghi đè [dispatchTouchEvent]
 * thì mọi sự kiện đi qua đây đều được xem xét trước khi chuyển tiếp.
 *
 * View này KHÔNG tiêu thụ sự kiện: kết quả luôn được chuyển tiếp nguyên vẹn cho lớp trên, nên
 * cuộn, bấm nút và mở liên kết bên trong vẫn hoạt động y như cũ.
 */
class SwipeBackLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    /** Gọi khi người dùng vuốt ngang sang phải. */
    var onSwipeRight: (() -> Unit)? = null

    private val minDistancePx: Int =
        resources.getDimensionPixelSize(R.dimen.swipe_back_min_distance)

    private var downX = 0f
    private var downY = 0f
    private var isHorizontalCandidate = false

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                isHorizontalCandidate = true
            }

            MotionEvent.ACTION_MOVE -> {
                // Ngón tay đã đi xa hơn theo chiều dọc: đây là cuộn dọc, không phải vuốt ngang.
                if (abs(event.y - downY) > abs(event.x - downX)) {
                    isHorizontalCandidate = false
                }
            }

            MotionEvent.ACTION_UP -> {
                val dx = event.x - downX
                val dy = abs(event.y - downY)
                if (isHorizontalCandidate && dx >= minDistancePx && dy <= dx * MAX_VERTICAL_RATIO) {
                    onSwipeRight?.invoke()
                }
                isHorizontalCandidate = false
            }

            // Huỷ cử chỉ khi hệ thống ngắt, hoặc khi có ngón thứ hai đặt xuống: thao tác nhiều
            // ngón không phải là vuốt về Main.
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN ->
                isHorizontalCandidate = false

            else -> Unit
        }

        // Chỉ để nhận biết cử chỉ, không quyết định việc tiêu thụ sự kiện.
        return super.dispatchTouchEvent(event)
    }

    companion object {
        /**
         * Ngón tay phải đi ngang ít nhất gấp đôi quãng dọc thì mới coi là vuốt ngang.
         *
         * Có ngưỡng này vì thao tác cuộn danh sách thường không đi thẳng đứng hoàn toàn: nếu chỉ
         * so sánh ngang-dọc thì một cú cuộn chéo sẽ bị hiểu nhầm thành vuốt về Main.
         */
        private const val MAX_VERTICAL_RATIO = 0.5f
    }
}
