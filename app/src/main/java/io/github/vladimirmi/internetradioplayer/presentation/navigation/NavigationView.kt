package io.github.vladimirmi.internetradioplayer.presentation.navigation

import io.github.vladimirmi.internetradioplayer.navigation.NavigationScreen
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

interface NavigationView : BackPressListener {

    fun navigateTo(screen: NavigationScreen)

    fun back()

}