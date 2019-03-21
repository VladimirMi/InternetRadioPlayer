package io.github.vladimirmi.internetradioplayer.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.setTextOrHide
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.NavigationScreen
import io.github.vladimirmi.internetradioplayer.navigation.NavigationTree
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.search.SearchStationsAdapter
import kotlinx.android.synthetic.main.fragment_navigation.*
import timber.log.Timber
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class NavigationFragment : BaseFragment<DataPresenter, DataView>(), DataView, NavigationView {

    companion object {
        private const val EXTRA_NAV_SCREEN = "EXTRA_NAV_SCREEN"

        fun newInstance(screen: NavigationScreen): NavigationFragment {
            val args = Bundle().apply { putString(EXTRA_NAV_SCREEN, screen.title) }
            return NavigationFragment().apply { arguments = args }
        }
    }

    override val layout = R.layout.fragment_navigation
    private lateinit var navigationScreen: NavigationScreen
    private val dataAdapter = SearchStationsAdapter()

    override fun providePresenter(): DataPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(DataPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = arguments?.getString(EXTRA_NAV_SCREEN) ?: return
        navigationScreen = NavigationTree.findScreen(key)
    }

    override fun onStart() {
        super.onStart()
        presenter.fetchData(navigationScreen.endpoint, navigationScreen.query)
    }

    override fun setupView(view: View) {
        setupChildren()
        setupData()
        parentBt.setTextOrHide(navigationScreen.parent?.title)
        parentBt.setOnClickListener { back() }
    }

    private fun setupChildren() {
        val inflater = LayoutInflater.from(requireContext())
        val childrenContainer = childrenContainer
        for (child in navigationScreen.children) {
            val childView = child.createSmallView(inflater, childrenContainer)
            childView.setOnClickListener { navigateTo(child) }
            childrenContainer.addView(childView)
        }
    }

    private fun setupData() {
        dataRv.adapter = dataAdapter
        dataRv.layoutManager = LinearLayoutManager(requireContext())
    }

    //region =============== NavigationView ==============

    override fun navigateTo(screen: NavigationScreen) {
        (parentFragment as? NavigationView)?.navigateTo(screen)
    }

    override fun back() {
        (parentFragment as? NavigationView)?.back()
    }

    override fun handleBackPressed(): Boolean {
        return if (navigationScreen.parent != null) {
            back()
            true
        } else false
    }

    //endregion

    //region =============== DataView ==============

    override fun setData(data: List<StationSearchRes>) {
        dataAdapter.stations = data
        dataAdapter.onItemClickListener = { presenter.selectData(it) }
        dataRv.visible(true)
    }

    override fun selectData(uri: String) {
        val position = dataAdapter.selectStation(uri)
        dataRv.scrollToPosition(position)
    }

    override fun showLoading(show: Boolean) {
        Timber.e("showLoading: $show")
    }

    //endregion
}
