package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

interface SearchView : BaseView {

    fun setSuggestions(list: List<Suggestion>)
}
