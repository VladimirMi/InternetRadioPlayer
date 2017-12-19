package io.github.vladimirmi.radius.navigation

import ru.terrakok.cicerone.commands.Forward

/**
 * Created by Vladimir Mikhalev 04.12.2017.
 */

class Router : ru.terrakok.cicerone.Router() {

    companion object {
        const val MEDIA_LIST_SCREEN = "media_list_screen"
        const val STATION_SCREEN = "station_screen"
        const val ICON_PICKER_SCREEN = "icon_picker_screen"
    }

    fun skipToNext() {
        executeCommand(NextStation())
    }

    fun skipToPrevious() {
        executeCommand(PreviousStation())
    }
}

class NextStation : Forward(Router.STATION_SCREEN, null)
class PreviousStation : Forward(Router.STATION_SCREEN, null)