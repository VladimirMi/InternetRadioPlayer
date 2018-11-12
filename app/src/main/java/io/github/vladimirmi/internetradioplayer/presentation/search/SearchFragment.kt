package io.github.vladimirmi.internetradioplayer.presentation.search

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*
import timber.log.Timber
import toothpick.Toothpick
import android.widget.SearchView as SearchViewAndroid

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchFragment : BaseFragment<SearchPresenter, SearchView>(), SearchView,
        SearchViewAndroid.OnQueryTextListener, View.OnFocusChangeListener {

    override val layout = R.layout.fragment_search

    private val adapter = SearchSuggestionsAdapter()

    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        searchView.setIconifiedByDefault(false)

        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        searchView.setOnQueryTextFocusChangeListener(this)

        suggestionsRv.layoutManager = LinearLayoutManager(context)
        suggestionsRv.adapter = adapter
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        Timber.e("onQueryTextSubmit: $query")
        presenter.search(query)

        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        Timber.e("onQueryTextChange: $newText")
        presenter.querySuggestions(newText)

        return true
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        Timber.e("onFocusChange: hasFocus $hasFocus")
    }

    override fun setSuggestions(list: List<Suggestion>) {
        adapter.setData(list)
    }
}
