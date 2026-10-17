package com.example.news.ui.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.news.data.model.Article
import com.example.news.data.model.MarketSeries

/** Những dòng bấm được ở trang Tài khoản, dùng để phân biệt việc cần làm khi người dùng bấm. */
enum class ProfileRow {
    UPGRADE,
    CLEAR_SAVED_DATA,
    LANGUAGE,
}

/**
 * Một dòng trong danh sách của trang Tài khoản.
 *
 * Vì sao gom cả những khối cố định (thông tin cá nhân, nhãn nhóm) vào cùng một danh sách: hai
 * nhóm "đã lưu" dài ra theo số thứ người dùng lưu, nên cả màn phải là MỘT danh sách cuộn được.
 * Nếu tách phần cố định ra ngoài rồi lồng danh sách vào trong thì phải lồng vùng cuộn vào vùng
 * cuộn, cử chỉ sẽ chồng lên nhau.
 *
 * Chỉ chứa dữ liệu hiển thị; mã tài nguyên được đặt ngay tại đây vì đây là lớp giao diện, còn
 * ViewModel giữ dữ liệu thuần.
 */
sealed interface ProfileListItem {

    /** Khối thông tin cá nhân: ảnh đại diện, tên, email, số điện thoại. */
    data class Account(
        val name: String,
        val email: String,
        val phone: String,
    ) : ProfileListItem

    /** Nhãn đầu một nhóm, ví dụ "CÀI ĐẶT". */
    data class Section(@param:StringRes val titleRes: Int) : ProfileListItem

    /**
     * Một dòng bấm được.
     *
     * @param showChevron true nếu bấm vào sẽ mở màn khác, false nếu bấm vào là làm luôn một việc
     *   (như xoá dữ liệu). Mũi tên chỉ có nghĩa "sẽ mở tiếp", nên không được hiện ở dòng không
     *   mở đi đâu.
     */
    data class Row(
        val row: ProfileRow,
        @param:StringRes val titleRes: Int,
        @param:StringRes val summaryRes: Int,
        @param:DrawableRes val iconRes: Int?,
        val showChevron: Boolean,
    ) : ProfileListItem

    data class SavedChart(val series: MarketSeries) : ProfileListItem

    data class SavedArticle(val article: Article) : ProfileListItem

    /** Thông báo khi một nhóm chưa có gì. */
    data class Empty(@param:StringRes val messageRes: Int) : ProfileListItem
}
