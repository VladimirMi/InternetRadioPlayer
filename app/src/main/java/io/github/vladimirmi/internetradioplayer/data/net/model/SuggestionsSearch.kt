package io.github.vladimirmi.internetradioplayer.data.net.model

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

class SuggestionsSearch(val result: List<SuggestionResult>)

class SuggestionResult(val keyword: String, val type: String)
