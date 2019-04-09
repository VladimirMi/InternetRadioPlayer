package io.github.vladimirmi.internetradioplayer.presentation.navigation

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.search.ManualSearchFragment

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

object SearchNavigationTree {

    val rootScreen = SearchRootScreen {
        fragmentScreen(R.string.search_manual, ManualSearchFragment::class.java)
        screen(R.string.search_talks) {
            talksScreen("Arts/Culture/Entertainment", "ACE")
            talksScreen("Music")
            talksScreen("News")
            talksScreen("Other")
            talksScreen("Politics")
            talksScreen("Public")
            talksScreen("Sports")
        }
        screen(R.string.search_music) {
            stationsScreen("70's") { topSongsScreen() }
            stationsScreen("80's") { topSongsScreen() }
            stationsScreen("90's") { topSongsScreen() }
            stationsScreen("00's") { topSongsScreen() }
            stationsScreen("Adult Contemporary") { topSongsScreen() }
            stationsScreen("Alternative") { topSongsScreen() }
            stationsScreen("Christian") { topSongsScreen() }
            stationsScreen("Christmas") { topSongsScreen() }
            stationsScreen("Country") { topSongsScreen() }
            stationsScreen("Classical") { topSongsScreen() }
            stationsScreen("Country") { topSongsScreen() }
            screen("Electronic") {
                topSongsScreen()
                stationsScreen("Chill") { topSongsScreen() }
                stationsScreen("Dubstep") { topSongsScreen() }
                stationsScreen("House") { topSongsScreen() }
                stationsScreen("Industrial") { topSongsScreen() }
                stationsScreen("Techno") { topSongsScreen() }
                stationsScreen("Trance") { topSongsScreen() }
            }
            stationsScreen("Hip Hop") { topSongsScreen() }
            stationsScreen("Hit Music") { topSongsScreen() }
            stationsScreen("Indian") { topSongsScreen() }
            stationsScreen("Jazz") { topSongsScreen() }
            stationsScreen("Latin Hits") { topSongsScreen() }
            stationsScreen("Metal") { topSongsScreen() }
            stationsScreen("Oldies") { topSongsScreen() }
            stationsScreen("Rap") { topSongsScreen() }
            stationsScreen("Reggae") { topSongsScreen() }
            stationsScreen("Rock") { topSongsScreen() }
            stationsScreen("Roots") { topSongsScreen() }
            stationsScreen("Soul / R&B", "Soul") { topSongsScreen("Soul") }
            stationsScreen("Standards") { topSongsScreen() }
            stationsScreen("World") { topSongsScreen() }
        }
    }


    fun findScreen(id: String): ScreenContext {
        return rootScreen.findScreen { it.id == id }
                ?: throw IllegalStateException("Can not find screen with id $id")
    }
}

private object SearchRootScreen {

    operator fun invoke(init: ScreenContext.() -> Unit): ScreenContext {
        return ScreenContext(R.string.tab_search).apply { init() }
    }
}
