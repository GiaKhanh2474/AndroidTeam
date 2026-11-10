package com.vlu.newsapi.data.model

/**
 * Tên hiển thị của 3 chuyên mục chính.
 *
 * Backend chỉ trả về mã chuyên mục (`KINH_TE`, `CHINH_TRI`, `CONG_NGHE`); tên tiếng Việt nằm ở
 * phía ứng dụng để màn Main có thể dựng tiêu đề khối mà không phải gọi thêm API.
 */
object NewsCategories {

    val all: List<Category> = listOf(
        Category(CategoryIds.ECONOMY, "Kinh tế"),
        Category(CategoryIds.POLITICS, "Chính trị"),
        Category(CategoryIds.TECHNOLOGY, "Công nghệ"),
    )

    /** Tên hiển thị của một mã chuyên mục; mã lạ thì trả về chính mã đó. */
    fun nameOf(categoryId: String): String =
        all.firstOrNull { it.id == categoryId }?.name ?: categoryId
}
