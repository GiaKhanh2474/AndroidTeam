package com.example.news.data.mock

import com.example.news.data.model.MarketPoint
import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import com.example.news.data.model.SideArticleKind
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Dữ liệu MINH HOẠ cho trang Thị trường.
 *
 * CẢNH BÁO: các con số ở đây do người viết tự đặt ra, KHÔNG lấy từ bất kỳ nguồn nào.
 * Đặc tả ghi biểu đồ "cập nhật liên tục", nhưng dự án chưa chốt nguồn dữ liệu thị trường
 * thật, nên giao diện phải ghi rõ "Dữ liệu minh hoạ" và TUYỆT ĐỐI không được trình bày
 * các số này như số liệu thật.
 *
 * Các con số chỉ nhằm mục đích kiểm chứng cách vẽ biểu đồ và luồng điều hướng.
 */
object MockMarket {

    /** Biểu đồ mặc định hiện ở đầu trang khi người dùng chưa đánh dấu ưu tiên biểu đồ nào. */
    const val DEFAULT_FOLLOWED_ID = "usd-vnd"

    private const val POINT_COUNT = 12

    /**
     * Hình dáng đường đi của từng loại biểu đồ, tính theo tỉ lệ so với giá trị gốc.
     *
     * Ba hình khác nhau để các biểu đồ trông không giống hệt nhau khi cùng đặt cạnh nhau.
     */
    private val SHAPE_RISING = listOf(
        1.000f, 0.998f, 1.003f, 1.001f, 1.006f, 1.004f,
        1.009f, 1.007f, 1.012f, 1.018f, 1.016f, 1.024f,
    )
    private val SHAPE_FALLING = listOf(
        1.000f, 1.005f, 1.002f, 0.994f, 0.997f, 0.988f,
        0.991f, 0.983f, 0.986f, 0.978f, 0.981f, 0.975f,
    )
    private val SHAPE_VOLATILE = listOf(
        1.000f, 1.012f, 0.995f, 1.018f, 0.990f, 1.006f,
        1.021f, 0.992f, 1.014f, 1.002f, 1.019f, 1.009f,
    )

    /** Nhãn trục ngang: [POINT_COUNT] ngày gần nhất, cũ nhất trước. */
    private val dayLabels: List<String> = run {
        val format = SimpleDateFormat("dd/MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -(POINT_COUNT - 1))
        (0 until POINT_COUNT).map {
            val label = format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            label
        }
    }

    val series: List<MarketSeries> = listOf(
        series(
            id = "usd-vnd",
            name = "Tỷ giá USD/VND",
            unit = "VND",
            decimals = 0,
            base = 25_400f,
            shape = SHAPE_RISING,
        ),
        series(
            id = "eur-vnd",
            name = "Tỷ giá EUR/VND",
            unit = "VND",
            decimals = 0,
            base = 27_400f,
            shape = SHAPE_FALLING,
        ),
        series(
            id = "jpy-vnd",
            name = "Tỷ giá JPY/VND",
            unit = "VND",
            decimals = 1,
            base = 163.5f,
            shape = SHAPE_VOLATILE,
        ),
        series(
            id = "gold",
            name = "Giá vàng",
            unit = "triệu đồng/lượng",
            decimals = 1,
            base = 89.5f,
            shape = SHAPE_RISING,
        ),
        series(
            id = "ron95",
            name = "Giá xăng RON 95",
            unit = "đồng/lít",
            decimals = 0,
            base = 21_350f,
            shape = SHAPE_FALLING,
        ),
        series(
            id = "diesel",
            name = "Giá dầu Diesel",
            unit = "đồng/lít",
            decimals = 0,
            base = 19_780f,
            shape = SHAPE_VOLATILE,
        ),
    )

    val sideArticles: List<SideArticle> = listOf(
        SideArticle(
            id = "side-ticker",
            kind = SideArticleKind.NEWS_TICKER,
            title = "Dòng sự kiện tin vắn toàn cầu",
            summary = "Diễn biến nhanh trong ngày ở các thị trường lớn, cập nhật theo từng mốc thời gian.",
        ),
        SideArticle(
            id = "side-top-read",
            kind = SideArticleKind.TOP_READ,
            title = "Bảng xếp hạng bài báo nóng trong ngày",
            summary = "Những bài được đọc nhiều nhất, xếp theo số lượt đọc trong 24 giờ qua.",
        ),
        SideArticle(
            id = "side-analysis",
            kind = SideArticleKind.ANALYSIS,
            title = "Góc phân tích và khám phá mở rộng",
            summary = "Bài phân tích chuyên sâu hơn về bối cảnh phía sau các con số.",
        ),
    )

    fun seriesById(seriesId: String): MarketSeries? = series.firstOrNull { it.id == seriesId }

    private fun series(
        id: String,
        name: String,
        unit: String,
        decimals: Int,
        base: Float,
        shape: List<Float>,
    ): MarketSeries = MarketSeries(
        id = id,
        name = name,
        unit = unit,
        decimals = decimals,
        points = shape.mapIndexed { index, multiplier ->
            MarketPoint(
                label = dayLabels[index],
                value = base * multiplier,
            )
        },
    )
}
