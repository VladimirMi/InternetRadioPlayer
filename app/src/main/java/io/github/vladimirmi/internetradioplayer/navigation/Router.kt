package io.github.vladimirmi.internetradioplayer.navigation

import io.github.vladimirmi.internetradioplayer.model.entity.Station
import ru.terrakok.cicerone.commands.Forward

/**
 * Created by Vladimir Mikhalev 04.12.2017.
 */

class Router : ru.terrakok.cicerone.Router() {

    companion object {
        const val GET_STARTED_SCREEN = "get_started_screen"
        const val MEDIA_LIST_SCREEN = "media_list_screen"
        const val STATION_SCREEN = "station_screen"
        const val ICON_PICKER_SCREEN = "icon_picker_screen"
        const val DELIMITER = "$"
    }

    fun skipToNext(station: Station) {
        executeCommand(NextStation("$STATION_SCREEN$DELIMITER${station.id}"))
    }

    fun skipToPrevious(station: Station) {
        executeCommand(PreviousStation("$STATION_SCREEN$DELIMITER${station.id}"))
    }

    fun showStationSlide(station: Station) {
        navigateTo("$STATION_SCREEN$DELIMITER${station.id}")
    }

    fun showStationReplace(station: Station) {
        executeCommand(ForwardReplace("$STATION_SCREEN$DELIMITER${station.id}"))
    }
}

class NextStation(screenKey: String) : Forward(screenKey, null)
class PreviousStation(screenKey: String) : Forward(screenKey, null)
class ForwardReplace(screenKey: String) : Forward(screenKey, null)