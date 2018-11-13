package io.github.vladimirmi.internetradioplayer.presentation.search

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.extensions.waitForLayout
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*
import toothpick.Toothpick
import androidx.appcompat.widget.SearchView as SearchViewAndroid

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchFragment : BaseFragment<SearchPresenter, SearchView>(), SearchView,
        SearchViewAndroid.OnQueryTextListener, View.OnFocusChangeListener, SearchSuggestionsAdapter.Callback {

    override val layout = R.layout.fragment_search

    private val suggestionsAdapter = SearchSuggestionsAdapter(this)

    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        suggestionsRv.layoutManager = LinearLayoutManager(context)
        suggestionsRv.adapter = suggestionsAdapter
        view.requestFocus()

        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(this)
        searchView.setOnQueryTextFocusChangeListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        presenter.search(query)
        constraintLayout.requestFocus()
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        presenter.querySuggestions(newText)
        return true
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        adjustSuggestionsRecyclerHeight(hasFocus)
        if (hasFocus) {
            presenter.querySuggestions(searchView.query.toString())
        } else {
            suggestionsAdapter.setData(emptyList())
        }
    }

    override fun onSuggestionSelected(suggestion: Suggestion) {
        searchView.setQuery(suggestion.value, false)
    }

    override fun setSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.setData(list)
    }

    private fun adjustSuggestionsRecyclerHeight(keyboardDisplayed: Boolean) {
        if (keyboardDisplayed) {
            val rect = Rect()
            suggestionsRv.getWindowVisibleDisplayFrame(rect)
            constraintLayout.waitForLayout {
                val oldVisibleHeight = rect.bottom - rect.top
                suggestionsRv.getWindowVisibleDisplayFrame(rect)
                val newVisibleHeight = rect.bottom - rect.top
                if (oldVisibleHeight == newVisibleHeight) return@waitForLayout false
                val xy = IntArray(2)
                suggestionsRv.getLocationInWindow(xy)

                val lp = suggestionsRv.layoutParams
                lp.height = rect.bottom - xy[1]
                suggestionsRv.layoutParams = lp
                return@waitForLayout true
            }
        } else {
            val lp = suggestionsRv.layoutParams
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            suggestionsRv.layoutParams = lp
        }
    }
}
