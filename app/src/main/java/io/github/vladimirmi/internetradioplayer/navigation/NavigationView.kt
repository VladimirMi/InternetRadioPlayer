package io.github.vladimirmi.internetradioplayer.navigation

import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

interface NavigationView : BackPressListener {

    fun navigateTo(screen: NavigationScreen)

    fun back()
}