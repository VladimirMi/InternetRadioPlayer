package io.github.vladimirmi.internetradioplayer.navigation

import ru.terrakok.cicerone.commands.Forward

/**
 * Created by Vladimir Mikhalev 04.12.2017.
 */

class Router : ru.terrakok.cicerone.Router() {

    companion object {
        const val GET_STARTED_SCREEN = "get_started_screen"
        const val STATIONS_LIST_SCREEN = "media_list_screen"
        const val STATION_SCREEN = "station_screen"
        const val ICON_PICKER_SCREEN = "icon_picker_screen"
        const val SETTINGS_SCREEN = "settings_screen"
        const val DELIMITER = "$"
    }

    fun skipToNext(id: String) {
        executeCommands(NextStation("$STATION_SCREEN$DELIMITER$id"))
    }

    fun skipToPrevious(id: String) {
        executeCommands(PreviousStation("$STATION_SCREEN$DELIMITER$id"))
    }

    fun showStationSlide(id: String) {
        navigateTo("$STATION_SCREEN$DELIMITER$id")
    }

    fun showStationReplace(id: String) {
        executeCommands(ForwardReplace("$STATION_SCREEN$DELIMITER$id"))
    }
}

class NextStation(screenKey: String) : Forward(screenKey, null)
class PreviousStation(screenKey: String) : Forward(screenKey, null)
class ForwardReplace(screenKey: String) : Forward(screenKey, null)
