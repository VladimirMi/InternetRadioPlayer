package io.github.vladimirmi.internetradioplayer.presentation.search

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import kotlinx.android.synthetic.main.fragment_search.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchFragment : BaseFragment<SearchPresenter, SearchView>(), SearchView {

    override val layout = R.layout.fragment_search

    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        val searchManager = context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val componentName = ComponentName(context!!, RootActivity::class.java)
        searchView.setIconifiedByDefault(false)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.suggestionsAdapter
    }
}
