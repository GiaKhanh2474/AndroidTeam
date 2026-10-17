package com.example.news.ui.info

import androidx.annotation.StringRes
import com.example.news.R

/**
 * Các trang thông tin mở từ danh sách dòng ở màn Tài khoản.
 *
 * Mỗi mục tự biết tiêu đề và nội dung của mình, nên màn hình chỉ cần nhận một khoá rồi tra ra,
 * không phải truyền chữ qua đối số điều hướng.
 *
 * Hiện tất cả đều là chỗ đang chờ nội dung thật. Khi nối được nguồn thật (tài khoản, bản dịch)
 * thì thay nội dung của đúng mục đó, không phải đụng tới màn hình.
 */
enum class InfoPage(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
) {
    UPGRADE(R.string.info_page_upgrade_title, R.string.info_page_upgrade_body),
    LANGUAGE(R.string.info_page_language_title, R.string.info_page_language_body),

    /** Lưới an toàn khi khoá truyền vào không khớp mục nào. */
    UNKNOWN(R.string.info_page_unknown_title, R.string.info_page_unknown_body),
    ;

    companion object {
        fun fromKey(key: String?): InfoPage =
            entries.firstOrNull { it.name == key } ?: UNKNOWN
    }
}
