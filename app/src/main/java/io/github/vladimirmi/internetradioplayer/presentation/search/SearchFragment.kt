package io.github.vladimirmi.internetradioplayer.presentation.search

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.extensions.waitForLayout
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.fragment_search.*
import toothpick.Toothpick
import androidx.appcompat.widget.SearchView as SearchViewAndroid

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchFragment : BaseFragment<SearchPresenter, SearchView>(), SearchView,
        View.OnFocusChangeListener, SearchSuggestionsAdapter.Callback {

    override val layout = R.layout.fragment_search

    private val suggestionsAdapter = SearchSuggestionsAdapter(this)

    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        val lm = LinearLayoutManager(context)
        suggestionsRv.layoutManager = lm
        suggestionsRv.adapter = suggestionsAdapter
        suggestionsRv.addItemDecoration(DividerItemDecoration(suggestionsRv.context, lm.orientation))
        suggestionsRv.visible(false)

        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextFocusChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        presenter.setSearchViewObservable(getSearchViewObservable())
    }

    override fun onSuggestionSelected(suggestion: Suggestion) {
        searchView.setQuery(suggestion.value, false)
    }

    override fun addRecentSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.addRecentSuggestions(list)
        suggestionsRv.scrollToPosition(0)
    }

    override fun addRegularSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.addRegularSuggestions(list)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        adjustSuggestionsRecyclerHeight(hasFocus)
        suggestionsRv.visible(hasFocus)
    }

    private fun getSearchViewObservable(): Observable<SearchEvent> {
        return Observable.create<SearchEvent> { e ->
            var queryListener: SearchViewAndroid.OnQueryTextListener? = object : SearchViewAndroid.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (!e.isDisposed) e.onNext(SearchEvent.Submit(query))
                    constraintLayout.requestFocus()
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (!e.isDisposed) e.onNext(SearchEvent.Change(newText))
                    return true
                }
            }

            if (!e.isDisposed) e.onNext(SearchEvent.Change(searchView.query.toString()))
            searchView.setOnQueryTextListener(queryListener)
            e.setDisposable(Disposables.fromAction {
                queryListener = null; searchView.setOnQueryTextListener(null)
            })
        }.share()
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
