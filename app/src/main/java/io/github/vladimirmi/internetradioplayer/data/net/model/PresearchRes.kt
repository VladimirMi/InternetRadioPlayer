package io.github.vladimirmi.internetradioplayer.data.net.model

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

class PresearchRes(val success: Boolean, val result: List<SuggestionRes>)

class SuggestionRes(val keyword: String, val type: String)
