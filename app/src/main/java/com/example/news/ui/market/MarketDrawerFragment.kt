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
import com.example.news.ui.common.APP_LOCALE
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.abs

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
 * Thao tác vuốt từ trái sang phải để đóng là phần cử chỉ, sẽ làm ở bước riêng; hiện tại
 * đóng bằng [☰], [✕] hoặc nút Back hệ thống.
 */
class MarketDrawerFragment : Fragment() {

    private var _binding: FragmentMarketDrawerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarketViewModel by viewModels { MarketViewModel.factory() }

    private val percentFormat: NumberFormat =
        NumberFormat.getNumberInstance(APP_LOCALE).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
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
        binding.tvFollowedChartChange.text = changeText(series)
        binding.tvFollowedChartRange.text = getString(
            R.string.market_range_label,
            series.points.first().label,
            series.points.last().label,
        )
        binding.chartFollowed.contentDescription = getString(
            R.string.market_chart_content_desc,
            series.name,
            formatValue(series, series.latest),
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
            row.tvSeriesMeta.text = getString(
                R.string.market_series_meta,
                formatValue(series, series.latest),
                series.unit,
                changeText(series),
            )

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

    /** Ví dụ: "▲ 2,4% trong 12 ngày". Mũi tên cho biết tăng hay giảm trong khoảng đang vẽ. */
    private fun changeText(series: MarketSeries): String {
        val percent = percentFormat.format(abs(series.changePercent))
        val days = (series.points.size - 1).toString()
        return when {
            series.changePercent > FLAT_THRESHOLD ->
                getString(R.string.market_change_up, percent, days)

            series.changePercent < -FLAT_THRESHOLD ->
                getString(R.string.market_change_down, percent, days)

            else -> getString(R.string.market_change_flat, days)
        }
    }

    private fun formatValue(series: MarketSeries, value: Float): String =
        NumberFormat.getNumberInstance(APP_LOCALE).apply {
            minimumFractionDigits = series.decimals
            maximumFractionDigits = series.decimals
        }.format(value)

    private companion object {
        /** Dưới mức này coi như không thay đổi, tránh hiện "▲ 0,0%". */
        const val FLAT_THRESHOLD = 0.05f
    }
}
