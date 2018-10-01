package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import io.github.vladimirmi.internetradioplayer.extensions.Preference
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

const val CURRENT_STATION_ID_KEY = "CURRENT_STATION_ID"
const val BUFFER_LENGTH_KEY = "BUFFER_LENGTH"

class Preferences
@Inject constructor(context: Context) {

    var currentStationId: String by Preference(context, CURRENT_STATION_ID_KEY, "")
    var bufferLength: Int by Preference(context, BUFFER_LENGTH_KEY, 3)

}
