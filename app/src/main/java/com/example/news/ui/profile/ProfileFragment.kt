package com.example.news.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news.R
import com.example.news.data.model.Article
import com.example.news.data.model.MarketSeries
import com.example.news.databinding.FragmentProfileBinding
import com.example.news.detail.DetailActivity
import com.example.news.ui.common.returnToMain
import com.example.news.ui.info.InfoPage
import com.example.news.ui.info.InfoPageFragment
import com.example.news.ui.market.MarketNavArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Màn 5 — Tài khoản.
 *
 * Header đặc tả: [←] TÀI KHOẢN CÁ NHÂN [Đăng xuất].
 *
 * Thứ tự các khối theo thống nhất: thông tin cá nhân → nâng cấp → cài đặt → đã lưu.
 *
 * Hai nhóm "đã lưu" đọc thẳng từ cơ sở dữ liệu (Room) chứ không phải dữ liệu minh hoạ, nên đây
 * là nơi duy nhất nhìn thấy đầy đủ những gì người dùng đã lưu trong máy: bài báo (nhấn đúp ở
 * trang Chi tiết) và biểu đồ (nhấn ngôi sao ở màn Thị trường).
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.factory() }

    private val listAdapter: ProfileListAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ProfileListAdapter(
            onRowClick = ::onRowClicked,
            onSavedChartClick = ::openSavedChart,
            onSavedArticleClick = ::openSavedArticle,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vuốt từ trái sang phải để quay về Main. Không tiêu thụ sự kiện nên bấm dòng, bấm nút
        // và cuộn danh sách vẫn hoạt động như cũ.
        binding.root.onSwipeRight = { returnToMain() }

        binding.headerToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_back)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.headerToolbar.inflateMenu(R.menu.menu_profile)
        binding.headerToolbar.setOnMenuItemClickListener { item ->
            // Chưa có tài khoản thật nên chưa xử lý đăng xuất.
            if (item.itemId == R.id.action_logout) {
                Snackbar.make(binding.root, R.string.feature_unavailable, Snackbar.LENGTH_SHORT)
                    .show()
                true
            } else {
                false
            }
        }

        binding.listProfile.layoutManager = LinearLayoutManager(requireContext())
        binding.listProfile.adapter = listAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Bỏ qua trạng thái đang tải ban đầu để danh sách không nhấp nháy cảnh báo
                    // "chưa lưu gì" trong lúc cơ sở dữ liệu còn đang đọc.
                    if (!state.isLoading) {
                        listAdapter.submitList(buildItems(state))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Dựng danh sách dòng từ trạng thái.
     *
     * Việc đổi dữ liệu thành dòng hiển thị nằm ở đây chứ không ở ViewModel: mã tài nguyên là
     * chuyện của lớp giao diện, ViewModel giữ dữ liệu thuần.
     */
    private fun buildItems(state: ProfileUiState): List<ProfileListItem> = buildList {
        val profile = state.profile
        add(
            if (profile == null) {
                ProfileListItem.Empty(R.string.profile_not_signed_in)
            } else {
                ProfileListItem.Account(profile.displayName, profile.email, profile.phone)
            },
        )

        add(
            ProfileListItem.Row(
                row = ProfileRow.UPGRADE,
                titleRes = R.string.profile_upgrade_title,
                summaryRes = R.string.profile_upgrade_summary,
                iconRes = R.drawable.ic_upgrade,
                showChevron = true,
            ),
        )

        add(ProfileListItem.Section(R.string.profile_section_settings))
        add(
            ProfileListItem.Row(
                row = ProfileRow.CLEAR_SAVED_DATA,
                titleRes = R.string.profile_settings_clear_title,
                summaryRes = R.string.profile_settings_clear_summary,
                iconRes = R.drawable.ic_delete_forever,
                // Không mở màn nào, chỉ xoá dữ liệu, nên không hiện mũi tên.
                showChevron = false,
            ),
        )
        add(
            ProfileListItem.Row(
                row = ProfileRow.LANGUAGE,
                titleRes = R.string.profile_settings_language_title,
                summaryRes = R.string.profile_settings_language_summary,
                iconRes = R.drawable.ic_public,
                showChevron = true,
            ),
        )

        add(ProfileListItem.Section(R.string.profile_section_saved_charts))
        if (state.savedCharts.isEmpty()) {
            add(ProfileListItem.Empty(R.string.profile_saved_charts_empty))
        } else {
            state.savedCharts.forEach { add(ProfileListItem.SavedChart(it)) }
        }

        add(ProfileListItem.Section(R.string.profile_section_saved_articles))
        if (state.savedArticles.isEmpty()) {
            add(ProfileListItem.Empty(R.string.profile_saved_articles_empty))
        } else {
            state.savedArticles.forEach { add(ProfileListItem.SavedArticle(it)) }
        }
    }

    private fun onRowClicked(row: ProfileRow) {
        when (row) {
            ProfileRow.UPGRADE -> openInfoPage(InfoPage.UPGRADE)
            ProfileRow.LANGUAGE -> openInfoPage(InfoPage.LANGUAGE)
            ProfileRow.CLEAR_SAVED_DATA -> confirmClearSavedData()
        }
    }

    /** Các dòng "mở màn khác" đều mở chung một màn, chỉ khác khoá trang truyền sang. */
    private fun openInfoPage(page: InfoPage) {
        Navigation.findNavController(requireView()).navigate(
            R.id.action_profileFragment_to_infoPageFragment,
            Bundle().apply { putString(InfoPageFragment.ARG_PAGE, page.name) },
        )
    }

    /**
     * Mở biểu đồ đã lưu ở màn Thị trường, với biểu đồ đó ở đầu trang.
     *
     * Chỉ truyền cách xem cho lần mở này; KHÔNG đánh dấu lại ưu tiên, để thứ tự người dùng đã
     * xếp không bị đổi chỉ vì họ bấm xem một biểu đồ.
     */
    private fun openSavedChart(series: MarketSeries) {
        Navigation.findNavController(requireView()).navigate(
            R.id.action_profileFragment_to_marketDrawerFragment,
            Bundle().apply { putString(MarketNavArgs.FOCUSED_SERIES_ID, series.id) },
        )
    }

    private fun openSavedArticle(article: Article) {
        startActivity(DetailActivity.newIntent(requireContext(), article.id))
    }

    /**
     * Hỏi lại trước khi xoá.
     *
     * Không hoàn tác được nên phải xác nhận: bài đã lưu là công sức của người dùng và không có
     * nguồn nào khác để lấy lại (bài có thể đã bị gỡ khỏi trang báo).
     */
    private fun confirmClearSavedData() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_clear_confirm_title)
            .setMessage(R.string.profile_clear_confirm_message)
            .setNegativeButton(R.string.profile_clear_confirm_cancel, null)
            .setPositiveButton(R.string.profile_clear_confirm_ok) { _, _ -> clearSavedData() }
            .show()
    }

    private fun clearSavedData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.clearSavedData()
            Snackbar.make(binding.root, R.string.profile_clear_done, Snackbar.LENGTH_SHORT).show()
        }
    }
}
