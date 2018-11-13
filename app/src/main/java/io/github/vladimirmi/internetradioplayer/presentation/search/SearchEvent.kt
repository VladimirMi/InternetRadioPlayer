package io.github.vladimirmi.internetradioplayer.presentation.search

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

sealed class SearchEvent(val query: String) {

    class Change(query: String) : SearchEvent(query)
    class Submit(query: String) : SearchEvent(query)
}
