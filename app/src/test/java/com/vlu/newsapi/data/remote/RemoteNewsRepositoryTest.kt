package com.vlu.newsapi.data.remote

import com.vlu.newsapi.data.model.CategoryIds
import com.vlu.newsapi.data.repository.RemoteNewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/**
 * Kiểm tra lớp Retrofit với 4 API của backend, dùng MockWebServer nên không cần MongoDB.
 *
 * Các test này bám đúng hợp đồng JSON trong `newsapi-backend/src/controllers/news.controller.js`:
 * snake_case, `published_at` dạng ISO, API 1 dùng `main_grid_summary`, API 2 dùng `card_summary`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RemoteNewsRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteNewsRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = RemoteNewsRepository(
            api = RetrofitClient.create(server.url("/api/v1/").toString()),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun mainFeedUsesMainGridSummaryAndParsesIsoDate() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "kinhTe": [
                    {
                      "article_id": "art_kt_01",
                      "title": "Lãi suất điều hành tiếp tục ổn định",
                      "main_grid_summary": "Tóm tắt ngắn 15-20 chữ cho ô 3x3.",
                      "card_summary": "Tóm tắt dài 35-50 chữ cho thẻ ngang ở màn Chuyên mục.",
                      "full_content": "Nội dung đầy đủ.",
                      "image_url": "https://example.com/anh.jpg",
                      "original_url": "https://example.com/bai-1",
                      "source_name": "VnExpress",
                      "category": "KINH_TE",
                      "published_at": "2026-10-26T07:00:00Z",
                      "date_label": "26/10/2026",
                      "ai_score": 96,
                      "is_main_featured": true
                    }
                  ],
                  "chinhTri": [],
                  "congNghe": []
                }
                """.trimIndent(),
            ),
        )

        val blocks = repository.observeMainFeed().first()

        assertEquals(3, blocks.size)
        val economy = blocks.first { it.category.id == CategoryIds.ECONOMY }
        assertEquals("Kinh tế", economy.category.name)
        assertEquals(1, economy.articles.size)

        val article = economy.articles.single()
        assertEquals("art_kt_01", article.id)
        assertEquals("Tóm tắt ngắn 15-20 chữ cho ô 3x3.", article.summary)
        assertEquals("VnExpress", article.source)
        assertEquals("https://example.com/bai-1", article.sourceUrl)
        // 26/10/2026 07:00 UTC — chỉ kiểm tra đã đọc được ISO, không phụ thuộc múi giờ máy chạy test.
        assertTrue(article.publishedAt > 1_700_000_000_000L)

        val request = server.takeRequest()
        assertEquals("/api/v1/news/main-grids", request.path)
    }

    @Test
    fun categoryArticlesUseCardSummaryFromDateGroups() = runTest {
        server.enqueue(
            jsonResponse(
                """
                [
                  {
                    "date_group": "26/10/2026",
                    "articles": [
                      {
                        "article_id": "art_kt_01",
                        "title": "Lãi suất điều hành tiếp tục ổn định",
                        "main_grid_summary": "Tóm tắt ngắn.",
                        "card_summary": "Tóm tắt dài cho thẻ ngang.",
                        "full_content": null,
                        "image_url": null,
                        "original_url": "https://example.com/bai-1",
                        "source_name": "VnExpress",
                        "category": "KINH_TE",
                        "published_at": "2026-10-26T07:00:00Z",
                        "date_label": "26/10/2026",
                        "ai_score": 96,
                        "is_main_featured": true
                      }
                    ]
                  }
                ]
                """.trimIndent(),
            ),
        )

        val articles = repository.observeArticlesOf(CategoryIds.ECONOMY).first()

        assertEquals(1, articles.size)
        assertEquals("Tóm tắt dài cho thẻ ngang.", articles.single().summary)
        assertEquals(CategoryIds.ECONOMY, articles.single().categoryId)

        val request = server.takeRequest()
        assertEquals(CategoryIds.ECONOMY, request.requestUrl?.queryParameter("category"))
        assertEquals("1", request.requestUrl?.queryParameter("page"))
        assertEquals(
            NewsApiService.DEFAULT_PAGE_SIZE.toString(),
            request.requestUrl?.queryParameter("limit"),
        )
    }

    @Test
    fun detailReturnsFullContentAndNotFoundBecomesNull() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "article_id": "art_kt_01",
                  "title": "Lãi suất điều hành tiếp tục ổn định",
                  "main_grid_summary": "Tóm tắt ngắn.",
                  "card_summary": "Tóm tắt dài.",
                  "full_content": "Nội dung đầy đủ của bài báo.",
                  "image_url": "https://example.com/anh.jpg",
                  "original_url": "https://example.com/bai-1",
                  "source_name": "VnExpress",
                  "category": "KINH_TE",
                  "published_at": "2026-10-26T07:00:00Z",
                  "date_label": "26/10/2026",
                  "ai_score": 96,
                  "is_main_featured": true
                }
                """.trimIndent(),
            ),
        )
        server.enqueue(MockResponse().setResponseCode(404))

        val article = repository.getArticle("art_kt_01")
        assertEquals("Nội dung đầy đủ của bài báo.", article?.content)
        assertEquals(CategoryIds.ECONOMY, article?.categoryId)

        // 404 là "không có bài", không phải lỗi mạng.
        assertNull(repository.getArticle("khong_ton_tai"))
    }

    @Test
    fun searchEncodesVietnameseQueryAndMapsResults() = runTest {
        server.enqueue(
            jsonResponse(
                """
                [
                  {
                    "article_id": "art_kt_01",
                    "title": "Lãi suất điều hành tiếp tục ổn định",
                    "main_grid_summary": "Tóm tắt ngắn.",
                    "card_summary": "Tóm tắt dài cho kết quả tìm kiếm.",
                    "full_content": "Nội dung đầy đủ.",
                    "image_url": null,
                    "original_url": "https://example.com/bai-1",
                    "source_name": "VnExpress",
                    "category": "KINH_TE",
                    "published_at": "2026-10-26T07:00:00Z",
                    "date_label": "26/10/2026",
                    "ai_score": 96,
                    "is_main_featured": true
                  }
                ]
                """.trimIndent(),
            ),
        )

        val results = repository.searchArticles("lãi suất").first()

        assertEquals(1, results.size)
        assertEquals("Tóm tắt dài cho kết quả tìm kiếm.", results.single().summary)

        val request = server.takeRequest()
        assertEquals("/api/v1/news/search", request.requestUrl?.encodedPath)
        assertEquals("lãi suất", request.requestUrl?.queryParameter("q"))
    }

    @Test
    fun mainFeedPropagatesHttpErrorSoUiCanShowRetry() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        var receivedHttpError = false
        try {
            repository.observeMainFeed().first()
        } catch (_: HttpException) {
            receivedHttpError = true
        }

        assertTrue(receivedHttpError)
    }

    private fun jsonResponse(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(body)
}
