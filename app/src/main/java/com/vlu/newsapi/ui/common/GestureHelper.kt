package com.vlu.newsapi.ui.common

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import kotlin.math.abs

/**
 * Bộ cử chỉ dùng chung cho trang đọc bài (Mục 3.2 của đặc tả).
 *
 * Gom cả ba cử chỉ của trang Chi tiết vào một chỗ để Fragment chỉ còn việc nối dữ liệu:
 *
 *  1. NHẤN ĐÚP 2 lần -> [onDoubleTapSaveArticle] (lưu bài vào Room).
 *  2. Cuộn sâu rồi lướt nhẹ NGƯỢC LÊN -> hiện mũi tên mờ ↑; nhấn mũi tên -> về đầu bài.
 *  3. LƯỚT TRÁI SANG PHẢI -> [onSwipeBack] (thoát trang, song song với nút [←]).
 *
 * Vì sao nhận sự kiện chạm bằng `setOnTouchListener` trên chính [scrollDetail]: đây là view
 * cuộn bao trọn vùng đọc bài, nên nó thấy mọi thao tác trên nội dung. Listener trả về `false`
 * để sự kiện vẫn đi tiếp như cũ — nhờ vậy cuộn, bấm liên kết và bấm nút bên trong không bị
 * ảnh hưởng.
 *
 * Hai ngưỡng của mũi tên cố ý để dạng hằng số pixel theo đúng đặc tả (`scrollY > 900`,
 * `dy < -10`). Đây là quy tắc hiển thị chứ không phải kích thước giao diện, nên không đưa vào
 * `dimens.xml`; kích thước và độ mờ của chính nút thì vẫn lấy từ tài nguyên.
 *
 * @param scrollDetail khung cuộn đọc bài (`nested_scroll_detail`).
 * @param btnFadedUpArrow nút mũi tên mờ ↑ (`btn_faded_up_arrow`).
 * @param onDoubleTapSaveArticle gọi khi người dùng nhấn đúp 2 lần liên tiếp.
 * @param onSwipeBack gọi khi người dùng lướt từ trái sang phải.
 */
class GestureHelper(
    private val scrollDetail: NestedScrollView,
    private val btnFadedUpArrow: View,
    private val onDoubleTapSaveArticle: () -> Unit,
    private val onSwipeBack: () -> Unit,
) {

    private val gestureDetector = GestureDetector(
        scrollDetail.context,
        object : GestureDetector.SimpleOnGestureListener() {

            /** Cử chỉ 1 — nhấn đúp để lưu bài. */
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTapSaveArticle()
                return true
            }

            /** Cử chỉ 3 — lướt ngang sang phải để thoát trang. */
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                // Đúng điều kiện đặc tả: đi ngang > 120 và ngang nhiều hơn dọc.
                if (diffX > SWIPE_MIN_DISTANCE_PX && abs(diffX) > abs(diffY)) {
                    onSwipeBack()
                    return true
                }
                return false
            }
        },
    )

    /** Gắn cả ba cử chỉ vào view. Gọi một lần trong `onViewCreated`. */
    fun attach() {
        attachSwipeBackAndDoubleTap()
        attachFadedUpArrowBehavior()
    }

    fun attachSwipeBackAndDoubleTap() {
        scrollDetail.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            // Không tiêu thụ sự kiện: cuộn và bấm bên trong vẫn chạy bình thường.
            false
        }

    }

    fun attachFadedUpArrowBehavior() {
        // Cử chỉ 2 — mũi tên mờ chỉ hiện khi đã đọc sâu VÀ đang lướt ngược lên.
        scrollDetail.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            updateUpArrow(scrollY = scrollY, oldScrollY = oldScrollY)
        }

        btnFadedUpArrow.setOnClickListener {
            scrollDetail.smoothScrollTo(0, 0)
            btnFadedUpArrow.isVisible = false
        }
    }

    /**
     * Quy tắc hiện/ẩn mũi tên ↑ theo đặc tả.
     *
     *  - Đầu trang (`scrollY <= 200`) hoặc đang lướt tiếp xuống (`dy > 10`): ẩn hoàn toàn.
     *  - Đã đọc quá dài (`scrollY > 900`) VÀ lướt nhẹ ngược lên (`dy < -10`): hiện, mờ 0.45.
     */
    private fun updateUpArrow(scrollY: Int, oldScrollY: Int) {
        val dy = scrollY - oldScrollY
        when {
            scrollY <= TOP_THRESHOLD_PX || dy > SCROLL_DOWN_DELTA_PX ->
                btnFadedUpArrow.isVisible = false

            scrollY > LONG_ARTICLE_THRESHOLD_PX && dy < SCROLL_UP_DELTA_PX -> {
                btnFadedUpArrow.isVisible = true
                btnFadedUpArrow.alpha = ARROW_ALPHA
            }
        }
    }

    private companion object {
        /** Quãng ngang tối thiểu (px) để một thao tác lướt được coi là "thoát trang". */
        const val SWIPE_MIN_DISTANCE_PX = 120f

        /** Còn trong quãng này thì luôn ẩn mũi tên: người đọc chưa đi đâu xa. */
        const val TOP_THRESHOLD_PX = 200

        /** Phải đọc quá quãng này mới cần lối quay lại đầu bài. */
        const val LONG_ARTICLE_THRESHOLD_PX = 900

        /** Lướt xuống quá 10px: đang đọc tiếp, giấu mũi tên đi. */
        const val SCROLL_DOWN_DELTA_PX = 10

        /** Lướt lên quá 10px: người đọc muốn quay lại, mới hiện mũi tên. */
        const val SCROLL_UP_DELTA_PX = -10

        /** Độ mờ của mũi tên theo đặc tả. */
        const val ARROW_ALPHA = 0.45f
    }
}
