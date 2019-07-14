package io.github.vladimirmi.internetradioplayer.presentation.data

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.navigation.DataScreen
import io.github.vladimirmi.internetradioplayer.presentation.navigation.ScreenContext
import io.github.vladimirmi.internetradioplayer.presentation.navigation.SearchNavigationTree
import kotlinx.android.synthetic.main.fragment_data.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class DataFragment : BaseFragment<DataPresenter, DataView>(), DataView {

    companion object {
        private const val EXTRA_SCREEN_ID = "EXTRA_SCREEN_ID"

        fun newInstance(screenContext: ScreenContext): DataFragment {
            return DataFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_SCREEN_ID, screenContext.id)
                }
            }
        }
    }

    override val layout = R.layout.fragment_data
    private lateinit var screenContext: DataScreen
    private val dataAdapter = DataAdapter()

    override fun providePresenter(): DataPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(DataPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getInt(EXTRA_SCREEN_ID) ?: return
        screenContext = SearchNavigationTree.getScreen(id) as DataScreen
    }

    override fun onStart() {
        super.onStart()
        fetchData()
    }

    override fun setupView(view: View) {
        setupDataRecycler()
        setupSwipeToRefresh()
    }

    private fun setupDataRecycler() {
        dataRv.adapter = dataAdapter
        dataRv.layoutManager = LinearLayoutManager(requireContext())
        dataAdapter.onItemClickListener = { presenter.selectMedia(it) }
    }

    private fun setupSwipeToRefresh() {
        swipeToRefresh.setOnRefreshListener {
            fetchData()
            swipeToRefresh.isRefreshing = false
        }
    }

    private fun fetchData() {
        presenter.fetchData(screenContext.endpoint, screenContext.query)
    }

    //region =============== DataView ==============

    override fun setData(data: List<Media>) {
        dataAdapter.data = data
        dataRv.scrollToPosition(0)
        dataRv.visible(data.isNotEmpty())
    }

    override fun selectMedia(media: Media) {
        dataAdapter.selectMedia(media.uri)
    }

    override fun showLoading(loading: Boolean) {
        swipeToRefresh.isRefreshing = loading
    }

    //endregion
}
