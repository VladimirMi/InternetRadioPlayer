package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

sealed class Suggestion(val value: String)

class RecentSuggestion(value: String) : Suggestion(value)
class RegularSuggestion(value: String) : Suggestion(value)
