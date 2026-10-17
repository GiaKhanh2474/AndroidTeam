package com.example.news.ui.common

import android.content.Context
import com.example.news.R
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Định dạng ngày hiển thị.
 *
 * Dùng chung cho trang Chi tiết và trang Tài khoản: cùng một ngày mà hiện hai kiểu ở hai màn là
 * lỗi khó phát hiện.
 *
 * Định dạng theo [APP_LOCALE] cho khớp với cách định dạng số, vì toàn bộ giao diện đang là
 * tiếng Việt.
 */
object DateText {

    /** Ví dụ: "17/10". */
    fun dayMonth(context: Context, epochMillis: Long): String = SimpleDateFormat(
        context.getString(R.string.pattern_day_month),
        APP_LOCALE,
    ).format(Date(epochMillis))
}
