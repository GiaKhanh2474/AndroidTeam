package com.vlu.newsapi.ui.profile

import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.data.model.MarketSeries
import com.vlu.newsapi.data.model.SideArticle
import com.vlu.newsapi.data.model.UserProfile
import com.vlu.newsapi.data.repository.FavouriteChartRepository
import com.vlu.newsapi.data.repository.MarketRepository
import com.vlu.newsapi.data.repository.SavedArticleRepository
import com.vlu.newsapi.data.repository.SessionRepository
import com.vlu.newsapi.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun article(id: String) = Article(
        id = id,
        categoryId = "c",
        title = "Bài $id",
        summary = "",
        source = "s",
        publishedAt = 0L,
        sourceUrl = "https://example.com/$id",
    )

    @Test
    fun deleteSavedArticleByIdRemovesOnlyThatArticleFromState() = runTest {
        val saved = object : SavedArticleRepository {
            val items = MutableStateFlow(listOf(article("1"), article("2"), article("3")))
            override fun observeSavedArticles(): Flow<List<Article>> = items
            override fun observeSavedCount(): Flow<Int> = items.map { it.size }
            override suspend fun saveArticleOnDoubleTap(article: Article) = Unit
            override suspend fun remove(articleId: String) {
                items.value = items.value.filter { it.id != articleId }
            }
            override suspend fun clear() { items.value = emptyList() }
        }
        val favourites = object : FavouriteChartRepository {
            override fun observeFavouriteIds() = flowOf(emptyList<String>())
            override suspend fun promote(seriesId: String) = Unit
            override suspend fun remove(seriesId: String) = Unit
            override suspend fun clear() = Unit
        }
        val market = object : MarketRepository {
            override fun observeSeries() = flowOf(emptyList<MarketSeries>())
            override fun observeSideArticles() = flowOf(emptyList<SideArticle>())
        }
        val user = object : UserRepository {
            override fun observeProfile() = flowOf<UserProfile?>(null)
        }
        val session = object : SessionRepository {
            override fun isSignedIn() = true
            override suspend fun setSignedIn(signedIn: Boolean) = Unit
        }

        val model = ProfileViewModel(user, saved, favourites, session, market)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            model.uiState.collect {}
        }
        runCurrent()
        assertEquals(listOf("1", "2", "3"), model.uiState.value.savedArticles.map { it.id })

        model.deleteSavedArticleById("2")
        runCurrent()

        assertEquals(listOf("1", "3"), model.uiState.value.savedArticles.map { it.id })
    }
}
