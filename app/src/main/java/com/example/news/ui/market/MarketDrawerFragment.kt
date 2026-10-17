package com.example.news.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.data.model.MarketSeries
import com.example.news.data.model.SideArticle
import com.example.news.data.model.SideArticleKind
import com.example.news.databinding.FragmentMarketDrawerBinding
import com.example.news.databinding.ItemMarketChartRowBinding
import com.example.news.databinding.ItemMarketSideArticleBinding
import com.example.news.ui.common.MarketFormat
import com.example.news.ui.common.returnToMain
import kotlinx.coroutines.launch

/**
 * Màn 4 — Thị trường (mở từ nút [☰] ở trang Main).
 *
 * Header đặc tả: [☰] BIỂU ĐỒ & DANH MỤC MỞ RỘNG [✕]. Cả hai nút đều đóng màn này.
 *
 * Trang gồm 3 phần:
 *  - đầu trang: biểu đồ đang theo dõi, hiện đầy đủ;
 *  - giữa: các biểu đồ khác, mỗi dòng một ngôi sao để đưa lên đầu trang;
 *  - cuối: bài báo bên lề (tin vắn toàn cầu, bảng xếp hạng, góc phân tích).
 *
 * Các con số là DỮ LIỆU MINH HOẠ (xem [com.example.news.data.mock.MockMarket]); ngay dưới
 * tên biểu đồ có nhãn ghi rõ điều đó nên không có chỗ nào ngụ ý đây là số liệu thật.
 *
 * Thao tác vuốt từ trái sang phải để đóng cũng có: cử chỉ đó do lớp bọc ngoài cùng của màn
 * ([com.example.news.ui.common.SwipeBackLayout]) nhận biết và đưa thẳng về Main.
 */
class MarketDrawerFragment : Fragment() {

    private var _binding: FragmentMarketDrawerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarketViewModel by viewModels {
        MarketViewModel.factory(
            focusedSeriesId = arguments?.getString(MarketNavArgs.FOCUSED_SERIES_ID),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMarketDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vuốt từ trái sang phải để quay về Main, giống đặc tả của các màn khác. Không tiêu thụ
        // sự kiện nên bấm ngôi sao, bấm [✕] và cuộn trang vẫn hoạt động như cũ.
        binding.root.onSwipeRight = { returnToMain() }

        // Đặc tả: biểu tượng bên trái của màn này là 3 gạch, không phải mũi tên quay lại.
        binding.headerToolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_open_market)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.headerToolbar.inflateMenu(R.menu.menu_market_drawer)
        binding.headerToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_close) {
                Navigation.findNavController(view).navigateUp()
                true
            } else {
                false
            }
        }

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

    private fun render(state: MarketUiState) {
        renderFollowedChart(state.followed)
        renderOtherCharts(state)
        renderSideArticles(state.sideArticles)
    }

    /** Phần đầu trang: biểu đồ ưu tiên số 1 hiện đầy đủ. */
    private fun renderFollowedChart(series: MarketSeries?) {
        if (series == null) return

        binding.chartFollowed.submitSeries(series)
        binding.tvFollowedChartName.text = series.name
        binding.tvFollowedChartChange.text = MarketFormat.change(requireContext(), series)
        binding.tvFollowedChartRange.text = getString(
            R.string.market_range_label,
            series.points.first().label,
            series.points.last().label,
        )
        binding.chartFollowed.contentDescription = getString(
            R.string.market_chart_content_desc,
            series.name,
            MarketFormat.value(series, series.latest),
            series.unit,
        )
    }

    /**
     * Phần giữa: các biểu đồ còn lại. Nhấn ngôi sao thì biểu đồ đó nhảy lên đầu trang.
     *
     * Danh sách nhỏ và cố định nên dựng thẳng vào LinearLayout; cả trang đã nằm trong
     * ScrollView nên không lồng thêm một danh sách cuộn được vào đây.
     */
    private fun renderOtherCharts(state: MarketUiState) {
        binding.containerOtherCharts.removeAllViews()

        state.otherSeries.forEach { series ->
            val row = ItemMarketChartRowBinding.inflate(
                layoutInflater,
                binding.containerOtherCharts,
                false,
            )
            row.tvSeriesName.text = series.name
            row.tvSeriesMeta.text = MarketFormat.meta(requireContext(), series)

            val isMarked = series.id in state.favouriteIds
            row.btnStar.setImageResource(
                if (isMarked) R.drawable.ic_star else R.drawable.ic_star_border,
            )
            row.btnStar.contentDescription = getString(R.string.market_mark_favourite, series.name)
            row.btnStar.setOnClickListener { viewModel.markAsFollowed(series.id) }

            binding.containerOtherCharts.addView(row.root)
        }
    }

    /** Phần cuối trang: bài báo bên lề, ngoài luồng chính. */
    private fun renderSideArticles(articles: List<SideArticle>) {
        binding.containerSideArticles.removeAllViews()

        articles.forEach { article ->
            val row = ItemMarketSideArticleBinding.inflate(
                layoutInflater,
                binding.containerSideArticles,
                false,
            )
            row.ivSideIcon.setImageResource(iconOf(article.kind))
            row.tvSideTitle.text = article.title
            row.tvSideSummary.text = article.summary

            binding.containerSideArticles.addView(row.root)
        }
    }

    private fun iconOf(kind: SideArticleKind): Int = when (kind) {
        SideArticleKind.NEWS_TICKER -> R.drawable.ic_public
        SideArticleKind.TOP_READ -> R.drawable.ic_emoji_events
        SideArticleKind.ANALYSIS -> R.drawable.ic_trending_up
    }
}
