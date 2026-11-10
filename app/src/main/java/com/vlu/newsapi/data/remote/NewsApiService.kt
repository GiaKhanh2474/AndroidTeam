package com.vlu.newsapi.data.remote

import com.vlu.newsapi.data.remote.dto.ArticleDto
import com.vlu.newsapi.data.remote.dto.DateGroupDto
import com.vlu.newsapi.data.remote.dto.MainGridsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 4 API tin tức của backend (`newsapi-backend/src/routes/api.routes.js`).
 *
 * Tất cả hàm đều `suspend` nên Retrofit tự chạy trên luồng nền; lỗi mạng/HTTP được ném thành
 * exception và do Repository/ViewModel bắt lại để hiển thị trạng thái Error.
 *
 * Base URL có dấu `/` ở cuối (xem `BuildConfig.NEWS_API_BASE_URL`), nên các đường dẫn dưới đây
 * không bắt đầu bằng `/`.
 */
interface NewsApiService {

    /** API 1 — 3 bài mới nhất của mỗi chuyên mục cho ma trận 3×3. */
    @GET("news/main-grids")
    suspend fun getMainGrids(): MainGridsDto

    /** API 2 — bài của một chuyên mục, backend đã gom sẵn theo mốc ngày. */
    @GET("news/by-date")
    suspend fun getNewsByDate(
        @Query("category") category: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = DEFAULT_PAGE_SIZE,
    ): List<DateGroupDto>

    /** API 3 — nội dung đầy đủ của một bài. */
    @GET("news/detail/{id}")
    suspend fun getArticleDetail(@Path("id") articleId: String): ArticleDto

    /** API 4 — tìm kiếm theo từ khoá trong tiêu đề, tóm tắt và tên nguồn. */
    @GET("news/search")
    suspend fun searchArticles(@Query("q") query: String): List<ArticleDto>

    companion object {
        /** Backend giới hạn tối đa 100 bài/trang; 50 đủ cho một chuyên mục trong ngày. */
        const val DEFAULT_PAGE_SIZE = 50
    }
}
