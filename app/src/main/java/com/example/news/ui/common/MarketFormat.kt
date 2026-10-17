package com.example.news.ui.common

import android.content.Context
import com.example.news.R
import com.example.news.data.model.MarketSeries
import java.text.NumberFormat
import kotlin.math.abs

/**
 * Định dạng số liệu thị trường.
 *
 * Để dùng chung cho màn Thị trường và danh sách "biểu đồ đã lưu" ở màn Tài khoản: cùng một con
 * số mà hiện hai kiểu khác nhau ở hai màn là lỗi rất khó phát hiện, vì người dùng sẽ tưởng hai
 * màn đang nói về hai số khác nhau.
 *
 * Số được định dạng theo [APP_LOCALE] vì toàn bộ giao diện đang là tiếng Việt.
 */
object MarketFormat {

    /** Dưới mức này coi như không thay đổi, tránh hiện "▲ 0,0%". */
    private const val FLAT_THRESHOLD = 0.05f

    /** Ví dụ: "21.816". Đơn vị để riêng vì mỗi biểu đồ có một đơn vị khác nhau. */
    fun value(series: MarketSeries, value: Float): String =
        NumberFormat.getNumberInstance(APP_LOCALE).apply {
            minimumFractionDigits = series.decimals
            maximumFractionDigits = series.decimals
        }.format(value)

    /** Ví dụ: "▲ 2,4% trong 12 ngày". Mũi tên cho biết tăng hay giảm trong khoảng đang vẽ. */
    fun change(context: Context, series: MarketSeries): String {
        val percent = NumberFormat.getNumberInstance(APP_LOCALE).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format(abs(series.changePercent))
        val days = (series.points.size - 1).toString()

        return when {
            series.changePercent > FLAT_THRESHOLD ->
                context.getString(R.string.market_change_up, percent, days)

            series.changePercent < -FLAT_THRESHOLD ->
                context.getString(R.string.market_change_down, percent, days)

            else -> context.getString(R.string.market_change_flat, days)
        }
    }

    /** Ví dụ: "21.816 đồng/lít • ▼ 2,5% trong 12 ngày". */
    fun meta(context: Context, series: MarketSeries): String = context.getString(
        R.string.market_series_meta,
        value(series, series.latest),
        series.unit,
        change(context, series),
    )
}
