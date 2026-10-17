package com.example.news.data.repository

import com.example.news.data.model.Article
import kotlinx.coroutines.flow.Flow

/**
 * Bài báo người dùng đã lưu (nhấn đúp ở trang Chi tiết).
 *
 * Là dữ liệu người dùng tạo ra và lưu trên máy, nên tách khỏi [NewsRepository] (nguồn tin bên
 * ngoài): xoá dữ liệu tin tức không được làm mất bài người dùng đã lưu.
 */
interface SavedArticleRepository {

    /** Bài đã lưu, mới lưu trước. */
    fun observeSavedArticles(): Flow<List<Article>>

    /** Bài này đã được lưu chưa. */
    fun observeIsSaved(articleId: String): Flow<Boolean>

    /** Số bài đã lưu, dùng cho phần tóm tắt ở trang Tài khoản. */
    fun observeSavedCount(): Flow<Int>

    /**
     * Lưu nếu chưa lưu, bỏ lưu nếu đã lưu.
     *
     * @return trạng thái SAU khi đổi (true = đang được lưu), để giao diện báo đúng cho người
     *   dùng thay vì phải tự đoán.
     */
    suspend fun toggle(article: Article): Boolean

    suspend fun remove(articleId: String): Unit
}
