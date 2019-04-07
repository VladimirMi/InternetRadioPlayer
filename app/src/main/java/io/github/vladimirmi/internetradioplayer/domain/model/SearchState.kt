package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 07.04.2019.
 */

sealed class SearchState {

    object Loading : SearchState()
    class Data(val data: List<Media>) : SearchState()
    class Error(val error: Throwable) : SearchState()
}