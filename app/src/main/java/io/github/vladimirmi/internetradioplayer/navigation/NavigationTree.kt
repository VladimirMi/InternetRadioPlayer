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
            screen("A")
            screen("B")
        }
        screen("Music") {
            stationsScreen("70's") {
                screen("Top songs")
            }
            stationsScreen("80's")
            stationsScreen("90's")
            stationsScreen("00's")
            stationsScreen("Adult Contemporary")
            stationsScreen("Alternative")
            stationsScreen("Christian")
            stationsScreen("Christmas")
            stationsScreen("ClassicCountry")
            stationsScreen("Classical")
            stationsScreen("Country")
            screen("Electronic") {
                stationsScreen("Chill")
                stationsScreen("Dubstep")
                stationsScreen("House")
                stationsScreen("Industrial")
                stationsScreen("Techno")
                stationsScreen("Trance")
            }
            stationsScreen("Hip Hop")
            stationsScreen("Hit Music")
            stationsScreen("Indian")
            stationsScreen("Jazz")
            stationsScreen("Latin Hits")
            stationsScreen("Metal")
            stationsScreen("Oldies")
            stationsScreen("Rap")
            stationsScreen("Reggae")
            stationsScreen("Rock")
            stationsScreen("Roots")
            stationsScreen("Soul")
            stationsScreen("Standards")
            stationsScreen("World")
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
