package com.vlu.newsapi.data.repository

import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.Category
import com.vlu.newsapi.data.model.CategoryIds
import com.vlu.newsapi.data.model.CategoryWithArticles
import com.vlu.newsapi.data.model.NewsCategories
import com.vlu.newsapi.data.remote.NewsApiService
import com.vlu.newsapi.data.remote.dto.ArticleDto
import com.vlu.newsapi.data.remote.dto.ArticleDto.SummaryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * [NewsRepository] lấy dữ liệu thật từ 4 API tin tức của backend.
 *
 * Mọi lời gọi mạng đều chạy trên [Dispatchers.IO]. Nếu mạng lỗi hoặc backend trả 4xx/5xx,
 * exception được ném ra khỏi Flow; ViewModel bắt lại và chuyển UI sang trạng thái Error, còn
 * [CachedNewsRepository] riêng của trang Main sẽ thử đọc cache trước khi báo lỗi.
 */
class RemoteNewsRepository(
    private val api: NewsApiService,
) : NewsRepository {

    override fun observeCategories(): Flow<List<Category>> = flow {
        emit(NewsCategories.all)
    }

    override fun observeMainFeed(): Flow<List<CategoryWithArticles>> = flow {
        val response = api.getMainGrids()

        val blocks = listOf(
            blockOf(CategoryIds.ECONOMY, response.kinhTe),
            blockOf(CategoryIds.POLITICS, response.chinhTri),
            blockOf(CategoryIds.TECHNOLOGY, response.congNghe),
        )

        // Nếu cả ba khối đều rỗng thì coi như lần tải này thất bại để lớp cache có cơ hội dùng
        // 9 bài đã lưu, thay vì ghi đè cache bằng một ma trận trống.
        emit(if (blocks.any { it.articles.isNotEmpty() }) blocks else emptyList())
    }.flowOn(Dispatchers.IO)

    override fun observeArticlesOf(categoryId: String): Flow<List<Article>> = flow {
        val groups = api.getNewsByDate(
            category = categoryId,
            page = 1,
            limit = NewsApiService.DEFAULT_PAGE_SIZE,
        )

        // Backend có thể tách một mốc ngày sang hai trang; ở đây chỉ lấy trang đầu (50 bài) nên
        // gộp lại rồi sắp xếp giảm dần theo thời điểm đăng là đủ cho màn Chuyên mục.
        emit(
            groups.orEmpty()
                .flatMap { group -> group.articles.orEmpty() }
                .map { dto -> dto.toArticle(categoryId, SummaryField.CARD) }
                .sortedByDescending { article -> article.publishedAt },
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun getArticle(articleId: String): Article? =
        withContext(Dispatchers.IO) {
            try {
                api.getArticleDetail(articleId).toArticle(summaryField = SummaryField.CARD)
            } catch (exception: HttpException) {
                // 404 là "không có bài", không phải lỗi mạng; màn Chi tiết hiện thông báo riêng.
                if (exception.code() == 404) null else throw exception
            }
        }

    override fun searchArticles(query: String): Flow<List<Article>> = flow {
        emit(
            api.searchArticles(query)
                .orEmpty()
                .map { dto -> dto.toArticle(summaryField = SummaryField.CARD) },
        )
    }.flowOn(Dispatchers.IO)

    private fun blockOf(categoryId: String, articles: List<ArticleDto>?): CategoryWithArticles =
        CategoryWithArticles(
            category = Category(categoryId, NewsCategories.nameOf(categoryId)),
            articles = articles.orEmpty().map { dto ->
                dto.toArticle(categoryId, SummaryField.MAIN_GRID)
            },
        )
}
