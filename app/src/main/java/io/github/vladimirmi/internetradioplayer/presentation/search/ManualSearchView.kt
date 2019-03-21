package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

interface ManualSearchView : BaseView {

    fun addRecentSuggestions(list: List<Suggestion>)

    fun addRegularSuggestions(list: List<Suggestion>)

    fun setStations(stations: List<StationSearchRes>)

    fun setFavorites(favorites: FlatStationsList)

    fun selectStation(uri: String)

    fun selectSuggestion(suggestion: Suggestion)

    fun showLoading(loading: Boolean)

    fun showPlaceholder(show: Boolean)
}
