package io.github.vladimirmi.internetradioplayer.presentation.search.manual

import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.extensions.isVisible
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.extensions.waitForLayout
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.search.SearchStationsAdapter
import kotlinx.android.synthetic.main.fragment_search_manual.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class ManualSearchFragment : BaseFragment<ManualSearchPresenter, ManualSearchView>(), ManualSearchView {

    override val layout = R.layout.fragment_search_manual

    private val suggestionsAdapter = SearchSuggestionsAdapter()
    private val stationsAdapter = SearchStationsAdapter()


    override fun providePresenter(): ManualSearchPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(ManualSearchPresenter::class.java).also {
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
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            adjustSuggestionsRecyclerHeight(hasFocus)
            suggestionsRv.visible(hasFocus)
            presenter.intervalSearchEnabled = !hasFocus
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                presenter.submitSearch(query)
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                presenter.changeQuery(newText)
                return false
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
        suggestionsRv.itemAnimator = null
        suggestionsRv.addItemDecoration(DividerItemDecoration(context, lm.orientation))
        suggestionsAdapter.onItemClickListener = this::selectSuggestion
        suggestionsRv.visible(false)

        suggestionsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                searchView.isSelected = suggestionsRv.canScrollVertically(-1)
            }
        })
    }

    private fun setupSwipeToRefresh() {
        swipeToRefresh.setOnRefreshListener {
            presenter.submitSearch(searchView.query.toString())
            swipeToRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        suggestionsRv.clearOnScrollListeners()
        super.onDestroyView()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId != R.id.context_menu_suggestion) return false
        val selectedItem = suggestionsAdapter.longClickedItem
        if (item.itemId == R.id.context_menu_action_delete && selectedItem != null) {
            presenter.deleteRecentSuggestion(selectedItem, searchView.query.toString())
            return true
        }
        return false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && view != null) searchView.clearFocus()
        if (isPresenterInit) {
            presenter.intervalSearchEnabled = isVisibleToUser
            if (isVisibleToUser && placeholderView.isVisible) {
                presenter.submitSearch(searchView.query.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.intervalSearchEnabled = userVisibleHint
    }

    //region =============== ManualSearchView ==============

    override fun addRecentSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.addRecentSuggestions(list)
        adjustSuggestionsRecyclerHeight()
        suggestionsRv.scrollToPosition(0)
    }

    override fun addRegularSuggestions(list: List<Suggestion>) {
        suggestionsAdapter.addRegularSuggestions(list)
        adjustSuggestionsRecyclerHeight()
        suggestionsRv.scrollToPosition(0)
    }

    override fun setStations(stations: List<StationSearchRes>) {
        stationsAdapter.stations = stations
    }

    override fun setFavorites(favorites: FlatStationsList) {
        stationsAdapter.setFavorites(favorites)
    }

    override fun selectStation(uri: String) {
        val position = stationsAdapter.selectStation(uri)
        stationsRv.scrollToPosition(position)
    }

    override fun selectSuggestion(suggestion: Suggestion) {
        searchView.setQuery(suggestion.value, true)
    }

    override fun showLoading(loading: Boolean) {
        swipeToRefresh.isRefreshing = loading
    }

    override fun showPlaceholder(show: Boolean) {
        placeholderView.visible(show)
    }

    //endregion

    private fun adjustSuggestionsRecyclerHeight(keyboardDisplayed: Boolean) {
        if (keyboardDisplayed) {
            val rect = Rect()
            suggestionsRv.getWindowVisibleDisplayFrame(rect)
            frameLayout.waitForLayout {
                if (view == null) return@waitForLayout true
                val oldVisibleHeight = rect.bottom - rect.top
                suggestionsRv.getWindowVisibleDisplayFrame(rect)
                val newVisibleHeight = rect.bottom - rect.top
                if (oldVisibleHeight == newVisibleHeight) return@waitForLayout false

                adjustSuggestionsRecyclerHeight(rect)
                true
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
        val maxHeight = Math.min(visibleRect.bottom - xy[1], frameLayout.height)

        val heightSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        suggestionsRv.measure(widthSpec, heightSpec)

        val lp = suggestionsRv.layoutParams
        lp.height = suggestionsRv.measuredHeight
        suggestionsRv.layoutParams = lp
    }
}
