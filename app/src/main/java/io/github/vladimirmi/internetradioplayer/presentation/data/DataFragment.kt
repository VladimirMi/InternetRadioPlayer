package io.github.vladimirmi.internetradioplayer.presentation.data

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.NavigationTree
import io.github.vladimirmi.internetradioplayer.navigation.ScreenContext
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_data.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class DataFragment : BaseFragment<DataPresenter, DataView>(), DataView {

    companion object {
        private const val EXTRA_SCREEN_ID = "EXTRA_SCREEN_ID"

        fun newInstance(screenContext: ScreenContext): DataFragment {
            val args = Bundle().apply { putString(EXTRA_SCREEN_ID, screenContext.id) }
            return DataFragment().apply { arguments = args }
        }
    }

    override val layout = R.layout.fragment_data
    private lateinit var screenContext: ScreenContext
    private val dataAdapter = DataAdapter()

    override fun providePresenter(): DataPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(DataPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getString(EXTRA_SCREEN_ID) ?: return
        screenContext = NavigationTree.findScreen(id)
    }

    override fun onStart() {
        super.onStart()
        presenter.fetchData(screenContext.endpoint, screenContext.query)
    }

    override fun setupView(view: View) {
        dataRv.adapter = dataAdapter
        dataRv.layoutManager = LinearLayoutManager(requireContext())
    }

    //region =============== DataView ==============

    override fun setData(data: List<Media>) {
        dataAdapter.data = data
        dataAdapter.onItemClickListener = { presenter.selectMedia(it) }
        dataRv.visible(true)
    }

    override fun selectMedia(id: String) {
        val position = dataAdapter.selectMedia(id)
        dataRv.scrollToPosition(position)
    }

    override fun showLoading(loading: Boolean) {
        swipeToRefresh.isRefreshing = loading
    }

    //endregion
}
