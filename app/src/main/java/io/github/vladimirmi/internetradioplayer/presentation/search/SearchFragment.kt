package io.github.vladimirmi.internetradioplayer.presentation.search

import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*
import timber.log.Timber
import toothpick.Toothpick
import android.widget.SearchView as SearchViewAndroid

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchFragment : BaseFragment<SearchPresenter, SearchView>(), SearchView,
        SearchViewAndroid.OnQueryTextListener, SearchViewAndroid.OnSuggestionListener,
        View.OnClickListener, SearchViewAndroid.OnCloseListener {

    override val layout = R.layout.fragment_search

    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(this)
        searchView.setOnSuggestionListener(this)
        searchView.setOnSearchClickListener(this)
        searchView.setOnCloseListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Timber.e("onQueryTextSubmit: $query")
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Timber.e("onQueryTextChange: $newText")
        return false
    }

    override fun onSuggestionSelect(position: Int): Boolean {
        Timber.e("onSuggestionSelect: $position")
        return false
    }

    override fun onSuggestionClick(position: Int): Boolean {
        Timber.e("onSuggestionClick: $position")
        return false
    }

    override fun onClick(v: View?) {
        Timber.e("onClick: ")
    }

    override fun onClose(): Boolean {
        Timber.e("onClose: ")
        return false
    }
}
