package io.github.vladimirmi.internetradioplayer.presentation.search

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.extensions.dp
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.extensions.waitForLayout
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
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
    private val stationsAdapter = SearchStationsAdapter()


    override fun providePresenter(): SearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(SearchPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        setupSearchView()
        setupSuggestions()
        setupStations()
        setupSwipeToRefresh()
    }

    private fun setupSearchView() {
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextFocusChangeListener(this)

        searchView.setOnQueryTextListener(object : SearchViewAndroid.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                presenter.submitSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                presenter.changeQuery(newText)
                return true
            }
        })
    }

    private fun setupStations() {
        val lm = LinearLayoutManager(context)
        stationsRv.layoutManager = lm
        stationsRv.adapter = stationsAdapter

        stationsAdapter.onAddToFavListener = { presenter.switchFavorite() }
        stationsAdapter.onItemClickListener = { presenter.selectStation(it) }
    }

    private fun setupSuggestions() {
        val lm = LinearLayoutManager(context)
        suggestionsRv.layoutManager = lm
        suggestionsRv.adapter = suggestionsAdapter
        suggestionsRv.addItemDecoration(DividerItemDecoration(context, lm.orientation))
        suggestionsRv.visible(false)
    }

    private fun setupSwipeToRefresh() {
        swipeToRefresh.setOnRefreshListener {
            presenter.submitSearch(searchView.query.toString())
            swipeToRefresh.isRefreshing = false
        }
    }

    override fun onSuggestionSelected(suggestion: Suggestion) {
        searchView.setQuery(suggestion.value, true)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        adjustSuggestionsRecyclerHeight(hasFocus)
        suggestionsRv.visible(hasFocus)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && view != null) searchView.clearFocus()
        if (isPresenterInit) presenter.isViewVisible = isVisibleToUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.isViewVisible = userVisibleHint
    }

    //region =============== SearchView ==============

    override fun addRecentSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.addRecentSuggestions(list)
        adjustSuggestionsRecyclerHeight()
    }

    override fun addRegularSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.addRegularSuggestions(list)
        adjustSuggestionsRecyclerHeight()
    }

    override fun setStations(stations: List<StationSearchRes>) {
        stationsAdapter.stations = stations
    }

    override fun setFavorites(favorites: FlatStationsList) {
        stationsAdapter.setFavorites(favorites)
    }

    override fun selectStation(station: Station) {
        stationsAdapter.selectStation(station)
    }

    override fun showLoading(loading: Boolean) {
        swipeToRefresh.isRefreshing = loading
        if (loading) searchView.clearFocus()
    }

    //endregion

    private fun adjustSuggestionsRecyclerHeight(keyboardDisplayed: Boolean) {
        if (keyboardDisplayed) {
            val rect = Rect()
            suggestionsRv.getWindowVisibleDisplayFrame(rect)
            constraintLayout.waitForLayout {
                val oldVisibleHeight = rect.bottom - rect.top
                suggestionsRv.getWindowVisibleDisplayFrame(rect)
                val newVisibleHeight = rect.bottom - rect.top
                if (oldVisibleHeight == newVisibleHeight) return@waitForLayout false

                adjustSuggestionsRecyclerHeight(rect)
                return@waitForLayout true
            }
        } else {
            val lp = suggestionsRv.layoutParams
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            suggestionsRv.layoutParams = lp
        }
    }

    private fun adjustSuggestionsRecyclerHeight(rect: Rect? = null) {
        val visibleRect = rect ?: Rect().also {
            suggestionsRv.getWindowVisibleDisplayFrame(it)
        }
        val xy = IntArray(2)
        suggestionsRv.getLocationInWindow(xy)
        val marginBottom = 8 * context!!.dp
        val maxHeight = visibleRect.bottom - xy[1] - marginBottom

        val heightSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        suggestionsRv.measure(widthSpec, heightSpec)

        val lp = suggestionsRv.layoutParams
        lp.height = suggestionsRv.measuredHeight
        suggestionsRv.layoutParams = lp
    }
}
