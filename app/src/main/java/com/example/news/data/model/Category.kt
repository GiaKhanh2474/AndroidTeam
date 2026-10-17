package com.example.news.data.model

/**
 * Chuyên mục lớn trên màn Main.
 *
 * Đặc tả yêu cầu 3 chuyên mục: Kinh tế, Chính trị, Công nghệ. Mỗi chuyên mục là một
 * "Grid Tổng" chứa 3 "Grid Con" (3 bài báo tóm tắt).
 */
data class Category(
    val id: String,
    val name: String,
)

/**
 * Một "Grid Tổng" trên màn Main: chuyên mục kèm các bài báo tóm tắt của nó.
 */
data class CategoryWithArticles(
    val category: Category,
    val articles: List<Article>,
)
