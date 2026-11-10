package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.local.MainFeedArticleEntity
import com.vlu.newsapi.data.local.MainFeedDao
import com.vlu.newsapi.data.local.toMainFeed
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.Category
import com.vlu.newsapi.data.model.CategoryWithArticles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map

/**
 * Bọc [NewsRepository] để 9 bài của trang Chính còn đọc được khi mất mạng.
 *
 * Nguyên tắc: mạng là nguồn đúng, cache là lưới an toàn.
 *  - Lấy được dữ liệu mới -> ghi đè cache rồi hiện dữ liệu mới.
 *  - Mạng lỗi (hoặc trả về rỗng) -> hiện đúng 9 bài đã lưu từ lần cuối còn mạng.
 *
 * Chỉ trang Chính được cache theo yêu cầu. Trang Chuyên mục và Chi tiết vẫn đọc thẳng nguồn
 * tin: cache cả 27 bài của mọi chuyên mục là việc khác, chưa được yêu cầu và làm tăng dữ liệu
 * phải đồng bộ mà không có màn hình nào dùng tới khi offline.
 *
 * Cố tình KHÔNG cache theo kiểu "hiện cache trước rồi mới thay bằng mạng": cách đó làm màn hình
 * nháy nội dung cũ mỗi lần mở app dù mạng đang tốt.
 */
class CachedNewsRepository(
    private val remote: NewsRepository,
    private val mainFeedDao: MainFeedDao,
) : NewsRepository {

    override fun observeCategories(): Flow<List<Category>> = remote.observeCategories()

    override fun observeMainFeed(): Flow<List<CategoryWithArticles>> =
        remote.observeMainFeed()
            .map { feed ->
                if (feed.isEmpty()) {
                    // Nguồn tin trả về rỗng: coi như chưa có gì mới, đừng xoá cache đang dùng
                    // được, và hiện những gì đã lưu.
                    feedFromCache()
                } else {
                    mainFeedDao.replaceAll(feed.toCacheRows())
                    feed
                }
            }
            .catch {
                // Mất mạng hoặc nguồn tin lỗi. Vẫn phải có gì đó để người dùng đọc.
                emitAll(feedFromCacheFlow())
            }

    override fun observeArticlesOf(categoryId: String): Flow<List<Article>> =
        remote.observeArticlesOf(categoryId)

    override suspend fun getArticle(articleId: String): Article? = remote.getArticle(articleId)

    override fun searchArticles(query: String): Flow<List<Article>> = remote.searchArticles(query)

    private suspend fun feedFromCache(): List<CategoryWithArticles> =
        mainFeedDao.getAll().toMainFeed()

    private fun feedFromCacheFlow(): Flow<List<CategoryWithArticles>> =
        mainFeedDao.observeAll().map { rows -> rows.toMainFeed() }
}

/**
 * Trải ma trận 3×3 thành các dòng phẳng để ghi vào bảng.
 *
 * Vị trí khối và vị trí ô được chốt ngay tại đây, lúc dữ liệu còn đang đúng thứ tự; nếu để
 * lúc đọc mới suy ra thì không còn gì bảo đảm thứ tự đó nữa.
 */
private fun List<CategoryWithArticles>.toCacheRows(): List<MainFeedArticleEntity> {
    val cachedAt = System.currentTimeMillis()
    return flatMapIndexed { blockIndex, block ->
        block.articles.mapIndexed { slot, article ->
            MainFeedArticleEntity(
                id = article.id,
                categoryId = block.category.id,
                categoryName = block.category.name,
                categoryPosition = blockIndex,
                slot = slot,
                title = article.title,
                summary = article.summary,
                source = article.source,
                publishedAt = article.publishedAt,
                sourceUrl = article.sourceUrl,
                imageUrl = article.imageUrl,
                content = article.content,
                cachedAt = cachedAt,
            )
        }
    }
}
