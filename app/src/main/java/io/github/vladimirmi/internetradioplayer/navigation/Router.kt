package io.github.vladimirmi.internetradioplayer.navigation

import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 04.12.2017.
 */

class Router : ru.terrakok.cicerone.Router() {

    companion object {
        const val MAIN_SCREEN = "MAIN_SCREEN"
        const val ICON_PICKER_SCREEN = "ICON_PICKER_SCREEN"
        const val SETTINGS_SCREEN = "SETTINGS_SCREEN"
        const val ROOT_SCREEN = MAIN_SCREEN
        const val DELIMITER = "$"
    }

    fun navigateTo(navId: Int) {
        navigateTo(navId.toScreenKey())
    }

    fun newRootScreen(navId: Int) {
        newRootScreen(navId.toScreenKey())
    }

    fun replaceScreen(navId: Int) {
        replaceScreen(navId.toScreenKey())
    }

    private fun Int.toScreenKey(): String {
        return if (this == R.id.nav_settings) {
            "$SETTINGS_SCREEN$DELIMITER$this"
        } else {
            "$MAIN_SCREEN$DELIMITER$this"
        }
    }
}
