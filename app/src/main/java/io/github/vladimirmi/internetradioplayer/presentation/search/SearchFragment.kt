package io.github.vladimirmi.internetradioplayer.presentation.search

import android.view.View
import com.google.android.material.tabs.TabLayout
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.favorite.records.RecordsFragment
import io.github.vladimirmi.internetradioplayer.presentation.search.manual.ManualSearchFragment
import io.github.vladimirmi.internetradioplayer.utils.SimpleOnTabSelectedListener
import kotlinx.android.synthetic.main.fragment_search.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

class SearchFragment : BaseFragment<SearchPresenter, SearchView>(), SearchView {

    override val layout = R.layout.fragment_search

    private val onTabSelectedListener = object : SimpleOnTabSelectedListener() {
        override fun onTabSelected(tab: TabLayout.Tab) {
            presenter.selectTab(tab.position)
        }
    }

    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        navigationTl.addTab(navigationTl.newTab().apply { text = getString(R.string.tab_search) })
        navigationTl.addTab(navigationTl.newTab().apply { text = getString(R.string.tab_talk) })
        navigationTl.addTab(navigationTl.newTab().apply { text = getString(R.string.tab_music) })
        navigationTl.selectTab(null)
        navigationTl.addOnTabSelectedListener(onTabSelectedListener)
    }

    override fun showPage(position: Int) {
        val fragment = when (position) {
            0 -> ManualSearchFragment()
            1 -> RecordsFragment()
            2 -> RecordsFragment()
            else -> return
        }
        fragment.setUserVisibleHint(userVisibleHint)
        childFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()

        selectTab(position)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded) childFragmentManager.fragments.forEach { it.userVisibleHint = isVisibleToUser }
    }

    override fun selectTab(position: Int) {
        navigationTl.removeOnTabSelectedListener(onTabSelectedListener)
        navigationTl.getTabAt(position)?.select()
        navigationTl.addOnTabSelectedListener(onTabSelectedListener)
    }
}