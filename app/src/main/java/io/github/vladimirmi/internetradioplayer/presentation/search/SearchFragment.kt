package io.github.vladimirmi.internetradioplayer.presentation.search

import android.os.Bundle
import io.github.vladimirmi.internetradioplayer.presentation.navigation.NavigationHolderFragment
import io.github.vladimirmi.internetradioplayer.presentation.navigation.SearchNavigationTree

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

class SearchFragment : NavigationHolderFragment() {

    override val rootScreenContext = SearchNavigationTree.rootScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentScreenContext = SearchNavigationTree.getDefaultScreen()
    }
}