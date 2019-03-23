package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView
import io.github.vladimirmi.internetradioplayer.presentation.data.DataView

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

interface ManualSearchView : BaseView, DataView {

    fun addRecentSuggestions(list: List<Suggestion>)

    fun addRegularSuggestions(list: List<Suggestion>)

    fun selectSuggestion(suggestion: Suggestion)

    fun showPlaceholder(show: Boolean)
}
