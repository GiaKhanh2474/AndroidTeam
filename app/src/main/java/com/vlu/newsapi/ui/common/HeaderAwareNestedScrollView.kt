package com.vlu.newsapi.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import com.vlu.newsapi.R
import kotlin.math.abs

/**
 * Khung cuộn của trang Chính, có thêm khả năng báo QUÃNG KÉO của ngón tay.
 *
 * Vì sao cần: đặc tả đổi trạng thái đầu trang theo `scrollY`, nhưng khi dữ liệu ngắn hơn màn
 * hình thì khung này không cuộn được, `scrollY` mãi bằng 0 và trạng thái 2 không bao giờ hiện.
 * Người dùng vẫn "lướt xuống" như mô tả, nên phải vẫn phản ứng.
 *
 * Vì sao không dùng `setOnTouchListener` từ Fragment: sự kiện chạm đi từ trên xuống, các thẻ
 * bài báo bên trong đều bấm được nên chúng giữ luôn cú chạm và view cha không thấy gì. Ghi đè
 * [dispatchTouchEvent] thì mọi sự kiện đi qua đây đều được xem xét trước khi chuyển tiếp.
 *
 * View này KHÔNG tiêu thụ sự kiện: kết quả luôn được chuyển tiếp nguyên vẹn cho lớp trên, nên
 * cuộn, bấm thẻ và bấm tiêu đề chuyên mục vẫn hoạt động bình thường.
 *
 * Chỉ báo khi nội dung THẬT SỰ không cuộn được; lúc cuộn được thì sự kiện cuộn đã lo việc đổi
 * trạng thái và hai đường sẽ giành nhau.
 */
class HeaderAwareNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : NestedScrollView(context, attrs, defStyleAttr) {

    /**
     * Gọi khi ngón tay đã kéo đủ xa để đổi trạng thái đầu trang.
     *
     * @param collapsed true khi kéo LÊN (người dùng lướt xuống để đọc tiếp), false khi kéo XUỐNG.
     */
    var onHeaderDrag: ((collapsed: Boolean) -> Unit)? = null

    private val dragThresholdPx: Int =
        resources.getDimensionPixelSize(R.dimen.main_header_scroll_threshold)

    private var pointerDownY = 0f
    private var reportedCollapsed = false

    /**
     * Đồng bộ với trạng thái thật của đầu trang.
     *
     * Cần thiết vì đầu trang còn đổi theo sự kiện cuộn; nếu chỉ dựa vào lần báo gần nhất của
     * chính view này thì hai bên sẽ lệch nhau và thao tác kéo ngược lại bị bỏ qua.
     */
    fun syncHeaderState(collapsed: Boolean) {
        reportedCollapsed = collapsed
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> pointerDownY = event.y

            MotionEvent.ACTION_MOVE -> {
                val canScroll = canScrollVertically(-1) || canScrollVertically(1)
                if (!canScroll) {
                    // Ngón tay đi LÊN -> delta âm -> nội dung trôi lên -> trạng thái 2.
                    val delta = event.y - pointerDownY
                    if (abs(delta) >= dragThresholdPx) {
                        val collapsed = delta < 0f
                        if (collapsed != reportedCollapsed) {
                            // Báo đúng một lần mỗi chiều để hiệu ứng 150ms không bị chạy lại
                            // liên tục theo từng sự kiện MOVE.
                            reportedCollapsed = collapsed
                            onHeaderDrag?.invoke(collapsed)
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
