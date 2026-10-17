package com.example.news.data.repository

import com.example.news.data.model.Article
import com.example.news.data.model.Category
import com.example.news.data.model.CategoryWithArticles
import kotlinx.coroutines.flow.Flow

/**
 * Nguồn dữ liệu tin tức.
 *
 * UI và ViewModel CHỈ biết interface này. Bước này dùng bản minh hoạ
 * ([MockNewsRepository]); khi chốt được API thật sẽ thay bằng bản Retrofit mà không
 * phải sửa tầng giao diện.
 *
 * Trả về [Flow] ngay từ đầu để sau này việc gọi mạng bất đồng bộ không làm thay đổi
 * chữ ký hàm.
 */
interface NewsRepository {

    /** 3 chuyên mục lớn của màn Main. */
    fun observeCategories(): Flow<List<Category>>

    /** Nội dung màn Main: mỗi chuyên mục kèm các bài tóm tắt. */
    fun observeMainFeed(): Flow<List<CategoryWithArticles>>

    /** Bài báo của một chuyên mục, mới nhất trước (dùng cho trang Chuyên mục). */
    fun observeArticlesOf(categoryId: String): Flow<List<Article>>

    /** Một bài báo theo id (dùng cho trang Chi tiết). */
    suspend fun getArticle(articleId: String): Article?
}
