package io.github.vladimirmi.internetradioplayer.navigation

import io.github.vladimirmi.internetradioplayer.presentation.search.ManualSearchFragment

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

object NavigationTree {

    val rootScreen = RootScreen("Search") {
        screen("Manual search") {
            fragment(ManualSearchFragment::class.java)
        }
        screen("Talks") {
            talksScreen("Arts/Culture/Entertainment", "ACE")
            talksScreen("Music")
            talksScreen("News")
            talksScreen("Other")
            talksScreen("Politics")
            talksScreen("Public")
            talksScreen("Sports")
        }
        screen("Music") {
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


    fun findScreen(title: String): ScreenContext {
        return rootScreen.findScreen(title)
                ?: throw IllegalStateException("Can not find screen $title")
    }
}

private object RootScreen {

    operator fun invoke(title: String, init: ScreenContext.() -> Unit): ScreenContext {
        return ScreenContext(title, null).apply { init() }
    }
}
