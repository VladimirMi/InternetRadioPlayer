package io.github.vladimirmi.radius.navigation

import io.github.vladimirmi.radius.model.entity.Station
import ru.terrakok.cicerone.commands.Forward

/**
 * Created by Vladimir Mikhalev 04.12.2017.
 */

class Router : ru.terrakok.cicerone.Router() {

    companion object {
        const val MEDIA_LIST_SCREEN = "media_list_screen"
        const val STATION_SCREEN = "station_screen"
        const val DELIMITER = '$'
    }

    fun skipToNext(station: Station) {
        executeCommand(Next("$STATION_SCREEN$DELIMITER${station.id}"))
    }

    fun skipToPrevious(station: Station) {
        executeCommand(Previous("$STATION_SCREEN$DELIMITER${station.id}"))
    }

    fun showStation(station: Station) {
        navigateTo("$STATION_SCREEN$DELIMITER${station.id}")
    }
}

class Next(screenKey: String) : Forward(screenKey, null)
class Previous(screenKey: String) : Forward(screenKey, null)