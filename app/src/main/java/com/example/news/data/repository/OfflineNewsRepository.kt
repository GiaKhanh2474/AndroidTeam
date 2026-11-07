package com.example.news.data.repository

import com.example.news.data.model.Article

/** Đọc bản đã lưu trước, kể cả khi nguồn tin mất mạng hoặc đã gỡ bài. */
class OfflineNewsRepository(
    private val source: NewsRepository,
    private val findSavedArticle: suspend (String) -> Article?,
) : NewsRepository by source {
    override suspend fun getArticle(articleId: String): Article? =
        findSavedArticle(articleId) ?: source.getArticle(articleId)
}
