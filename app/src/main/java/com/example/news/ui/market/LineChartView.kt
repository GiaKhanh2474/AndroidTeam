package com.example.news.ui.market

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.R as AppCompatR
import com.example.news.R
import com.example.news.data.model.MarketSeries
import com.example.news.ui.common.APP_LOCALE
import com.google.android.material.color.MaterialColors
import java.text.NumberFormat
import kotlin.math.max

/**
 * Biểu đồ đường vẽ trực tiếp bằng [Canvas], KHÔNG dùng thư viện ngoài.
 *
 * Vẽ lưới ngang kèm nhãn giá trị ở lề trái, vùng tô chuyển sắc dưới đường dữ liệu, đường
 * dữ liệu và một chấm đánh dấu điểm mới nhất. Nhãn trục ngang chỉ vẽ điểm đầu, giữa và cuối
 * để không bị rối khi có nhiều điểm.
 *
 * Màu lấy từ theme (colorPrimary / colorOutline / colorOnSurfaceVariant) nên tự đúng ở cả
 * chế độ sáng và tối, không cần khai báo thuộc tính riêng.
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = themeColor(AppCompatR.attr.colorPrimary, Color.BLACK)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = themeColor(AppCompatR.attr.colorPrimary, Color.BLACK)
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = themeColor(com.google.android.material.R.attr.colorOutline, Color.LTGRAY)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.chart_label_text_size)
        color = themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
    }

    private val fillTopColor: Int = withAlpha(
        themeColor(AppCompatR.attr.colorPrimary, Color.BLACK),
        FILL_ALPHA,
    )

    private var series: MarketSeries? = null

    private var valueFormat: NumberFormat = NumberFormat.getNumberInstance(APP_LOCALE)

    /**
     * Nạp dữ liệu mới. Truyền null để xoá biểu đồ.
     *
     * Dữ liệu được truyền vào từ ngoài (không đọc trực tiếp từ repository) để view này
     * không phụ thuộc tầng dữ liệu và dễ kiểm thử riêng.
     */
    fun submitSeries(series: MarketSeries?) {
        this.series = series
        val decimals = series?.decimals ?: 0
        valueFormat = NumberFormat.getNumberInstance(APP_LOCALE).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val data = series ?: return
        if (data.points.size < 2) return

        val valueLabelWidth = widestValueLabelWidth(data)
        val labelHeight = labelPaint.descent() - labelPaint.ascent()
        val labelGap = 4f * density

        val left = paddingLeft + valueLabelWidth + 6f * density
        val right = width.toFloat() - paddingRight
        val top = paddingTop + labelGap
        val bottom = height.toFloat() - paddingBottom - labelHeight - labelGap

        if (right <= left || bottom <= top) return

        val values = data.points.map { it.value }
        var minValue = values.minOrNull() ?: 0f
        var maxValue = values.maxOrNull() ?: 0f
        if (minValue == maxValue) {
            minValue -= 1f
            maxValue += 1f
        }
        // Chừa 10% ở trên và dưới để đường dữ liệu không dính sát mép khung.
        val headroom = (maxValue - minValue) * 0.1f
        minValue -= headroom
        maxValue += headroom
        val span = maxValue - minValue

        // Lưới ngang kèm nhãn giá trị ở lề trái.
        for (index in 0..GRID_STEP_COUNT) {
            val y = top + (bottom - top) * index / GRID_STEP_COUNT
            canvas.drawLine(left, y, right, y, gridPaint)

            val value = maxValue - span * index / GRID_STEP_COUNT
            val text = valueFormat.format(value)
            canvas.drawText(
                text,
                left - 6f * density - labelPaint.measureText(text),
                y - (labelPaint.ascent() + labelPaint.descent()) / 2f,
                labelPaint,
            )
        }

        val stepX = (right - left) / (data.points.size - 1)
        val linePath = Path()
        val fillPath = Path()
        fillPath.moveTo(left, bottom)

        data.points.forEachIndexed { index, point ->
            val x = left + stepX * index
            val y = bottom - (point.value - minValue) / span * (bottom - top)
            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        fillPath.lineTo(right, bottom)
        fillPath.close()

        fillPaint.shader = LinearGradient(
            0f,
            top,
            0f,
            bottom,
            fillTopColor,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP,
        )
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)

        // Chấm tròn đánh dấu điểm mới nhất (điểm cuối luôn nằm ở mép phải).
        val lastY = bottom - (data.points.last().value - minValue) / span * (bottom - top)
        canvas.drawCircle(right, lastY, 4f * density, dotPaint)

        // Nhãn trục ngang: chỉ vẽ điểm đầu, điểm giữa và điểm cuối.
        val labelBaseline = height.toFloat() - paddingBottom - labelPaint.descent() - labelGap
        val lastIndex = data.points.size - 1
        listOf(0, lastIndex / 2, lastIndex).distinct().forEach { index ->
            val text = data.points[index].label
            val textWidth = labelPaint.measureText(text)
            val x = left + stepX * index
            val drawX = when (index) {
                0 -> x
                lastIndex -> x - textWidth
                else -> x - textWidth / 2f
            }
            canvas.drawText(text, drawX, labelBaseline, labelPaint)
        }
    }

    /** Bề rộng nhãn giá trị dài nhất, dùng để chừa lề trái cho vừa đủ. */
    private fun widestValueLabelWidth(data: MarketSeries): Float {
        val smallest = valueFormat.format(data.points.minOf { it.value })
        val largest = valueFormat.format(data.points.maxOf { it.value })
        return max(labelPaint.measureText(smallest), labelPaint.measureText(largest))
    }

    private fun themeColor(attr: Int, fallback: Int): Int =
        MaterialColors.getColor(this, attr, fallback)

    private fun withAlpha(color: Int, alpha: Float): Int = Color.argb(
        (255 * alpha).toInt(),
        Color.red(color),
        Color.green(color),
        Color.blue(color),
    )

    private companion object {
        const val GRID_STEP_COUNT = 4
        const val FILL_ALPHA = 0.22f
    }
}
