package com.vlu.newsapi.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.vlu.newsapi.R
import com.vlu.newsapi.data.model.Article
import com.vlu.newsapi.databinding.FragmentSearchBinding
import com.vlu.newsapi.ui.common.returnToMain
import com.vlu.newsapi.ui.detail.ArticleDetailFragment
import kotlinx.coroutines.launch

/**
 * Màn kết quả tìm kiếm — nơi dùng API 4 (`GET /news/search?q=...`).
 *
 * Được mở từ thanh tìm kiếm ở trạng thái 2 của màn Main. Cùng quy ước cử chỉ với màn Chuyên mục:
 * nút [←] lùi một bước, vuốt trái sang phải trở về Main.
 */
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val query: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_QUERY).orEmpty()
    }

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.factory(query)
    }

    private val adapter = SearchAdapter(onArticleClick = ::openArticleDetail)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.onSwipeRight = { returnToMain() }
        binding.btnBackSearch.setOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }
        binding.btnSearchRetry.setOnClickListener { viewModel.retry() }

        binding.tvSearchTitle.text = getString(R.string.search_result_title, query)

        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: SearchUiState) {
        binding.progressSearch.isVisible = state.isLoading

        val showMessage = !state.isLoading && (state.hasError || state.results.isEmpty())
        binding.searchMessageGroup.isVisible = showMessage
        binding.tvSearchMessage.setText(
            if (state.hasError) R.string.error_load_news else R.string.search_empty,
        )
        binding.btnSearchRetry.isVisible = state.hasError

        binding.rvSearchResults.isVisible =
            !state.isLoading && !state.hasError && state.results.isNotEmpty()

        adapter.submitList(state.results)
    }

    private fun openArticleDetail(article: Article) {
        Navigation.findNavController(requireView()).navigate(
            R.id.articleDetailFragment,
            Bundle().apply { putString(ArticleDetailFragment.ARG_ARTICLE_ID, article.id) },
        )
    }

    companion object {
        /** Từ khoá người dùng nhập ở màn Main, khớp khai báo trong nav_graph. */
        const val ARG_QUERY = "query"
    }
}
