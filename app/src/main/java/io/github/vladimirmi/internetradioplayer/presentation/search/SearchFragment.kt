package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.data.preference.Preferences
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.navigation.NavigationHolderFragment
import io.github.vladimirmi.internetradioplayer.presentation.navigation.ScreenContext
import io.github.vladimirmi.internetradioplayer.presentation.navigation.SearchNavigationTree

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

class SearchFragment : NavigationHolderFragment() {

    val preferences: Preferences by lazy {
        Scopes.app.getInstance(Preferences::class.java)
    }

    override fun getFirstScreen(): ScreenContext {
        val path = preferences.searchScreenPath
        return if (path.isEmpty()) SearchNavigationTree.getDefaultScreen()
        else SearchNavigationTree.getScreenByPath(path)
    }

    override fun onScreenChange(screen: ScreenContext) {
        preferences.searchScreenPath = screen.path
    }
}