package io.github.vladimirmi.internetradioplayer.navigation

import io.github.vladimirmi.internetradioplayer.presentation.search.manual.ManualSearchFragment

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
            screen("70s")
            screen("80s")
            screen("90s")
            screen("00s")
            screen("Adult Contemporary")
            screen("Alternative")
            screen("Christian")
            screen("Christmas")
            screen("ClassicCountry")
            screen("Classical")
            screen("Country")
            screen("Electronic") {
                screen("Chill")
                screen("Dubstep")
                screen("House")
                screen("Industrial")
                screen("Techno")
                screen("Trance")
            }
            screen("Hip Hop")
            screen("Hit Music")
            screen("Indian")
            screen("Jazz")
            screen("Latin Hits")
            screen("Metal")
            screen("Oldies")
            screen("Rap")
            screen("Reggae")
            screen("Rock")
            screen("Roots")
            screen("Soul")
            screen("Standards")
            screen("World")
        }
    }


    fun findScreen(title: String): NavigationScreen {
        return rootScreen.findScreen(title)
                ?: throw IllegalStateException("Can not find screen $title")
    }
}

private object RootScreen {

    operator fun invoke(title: String, init: NavigationScreen.() -> Unit): NavigationScreen {
        return NavigationScreen(title, null).apply { init() }
    }
}
