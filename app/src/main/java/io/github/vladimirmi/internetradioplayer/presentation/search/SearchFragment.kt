package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.presentation.navigation.NavigationHolderFragment
import io.github.vladimirmi.internetradioplayer.presentation.navigation.SearchNavigationTree

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

class SearchFragment : NavigationHolderFragment() {

    override val rootScreenContext = SearchNavigationTree.rootScreen
}