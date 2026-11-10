package com.vlu.newsapi.data.remote

import com.vlu.newsapi.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Tạo [NewsApiService] từ base URL trong `BuildConfig`.
 *
 * Base URL mặc định là `http://10.0.2.2:5000/api/v1/` (localhost của máy tính khi chạy emulator).
 * Khi backend đã deploy, build lại với `-PnewsApiBaseUrl=https://.../api/v1/`.
 */
object RetrofitClient {

    fun create(baseUrl: String = BuildConfig.NEWS_API_BASE_URL): NewsApiService {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val logging = HttpLoggingInterceptor().apply {
            // Chỉ log ở bản debug để không rò rỉ nội dung/URL trên bản release.
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 20L
}
