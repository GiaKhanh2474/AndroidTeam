package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.model.Article
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

    /** Số bài đã lưu, dùng cho phần tóm tắt ở trang Tài khoản. */
    fun observeSavedCount(): Flow<Int>

    /**
     * Lưu bài báo khi người dùng chạm đúp ở trang Chi tiết.
     *
     * Chỉ thêm, không bỏ lưu — đúng hợp đồng `saveArticleOnDoubleTap(article)` của đặc tả.
     * Chạm đúp lại lần nữa lên cùng bài thì ghi đè đúng bài đó, dữ liệu vẫn còn nguyên.
     */
    suspend fun saveArticleOnDoubleTap(article: Article)

    suspend fun remove(articleId: String): Unit

    /**
     * Xoá toàn bộ bài đã lưu.
     *
     * Dùng cho mục "Xoá dữ liệu đã lưu" ở trang Tài khoản, không dùng cho thao tác thường ngày.
     */
    suspend fun clear(): Unit
}
