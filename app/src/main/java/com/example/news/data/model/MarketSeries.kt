package com.example.news.data.model

/** Một điểm dữ liệu trên biểu đồ: nhãn trục ngang (ví dụ "17/10") và giá trị. */
data class MarketPoint(
    val label: String,
    val value: Float,
)

/**
 * Một chuỗi số liệu để vẽ biểu đồ (tỷ giá, giá vàng, giá xăng…).
 *
 * @param decimals số chữ số thập phân khi hiển thị: tỷ giá VND cần 0, giá vàng cần 1.
 */
data class MarketSeries(
    val id: String,
    val name: String,
    val unit: String,
    val decimals: Int,
    val points: List<MarketPoint>,
) {

    /** Giá trị mới nhất trong khoảng thời gian đang hiển thị. */
    val latest: Float get() = points.last().value

    /**
     * Mức thay đổi so với điểm đầu của khoảng đang hiển thị, tính theo phần trăm.
     *
     * Đây là mức thay đổi TRONG KHOẢNG ĐANG VẼ, không phải so với hôm qua.
     */
    val changePercent: Float
        get() {
            if (points.size < 2) return 0f
            val first = points.first().value
            if (first == 0f) return 0f
            return (points.last().value - first) / first * 100f
        }
}
