package com.example.news.ui.market

/**
 * Đối số điều hướng của màn Thị trường.
 *
 * Để ở một chỗ vì có hai nơi cùng dùng: màn Thị trường đọc đối số này, còn trang Tài khoản ghi
 * nó khi mở một biểu đồ đã lưu. Hai bên lệch tên khoá là lỗi chỉ lộ ra khi bấm vào, nên không
 * để mỗi bên tự viết chuỗi.
 */
object MarketNavArgs {

    /** Biểu đồ cần đưa lên đầu trang; để trống thì dùng đúng thứ tự ưu tiên đã lưu. */
    const val FOCUSED_SERIES_ID = "focusedSeriesId"
}
