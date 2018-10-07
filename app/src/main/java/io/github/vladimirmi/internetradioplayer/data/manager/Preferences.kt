package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import android.content.SharedPreferences
import io.github.vladimirmi.internetradioplayer.extensions.Preference
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

const val CURRENT_STATION_ID_KEY = "CURRENT_STATION_ID"
const val INITIAL_BUFFER_LENGTH_KEY = "INITIAL_BUFFER_LENGTH"
const val BUFFER_LENGTH_KEY = "BUFFER_LENGTH"

class Preferences
@Inject constructor(context: Context) {

    val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("default", Context.MODE_PRIVATE)
    }
    var currentStationId: String by Preference(sharedPreferences, CURRENT_STATION_ID_KEY, "")
    var initialBufferLength: Int by Preference(sharedPreferences, INITIAL_BUFFER_LENGTH_KEY, 3)
    var bufferLength: Int by Preference(sharedPreferences, BUFFER_LENGTH_KEY, 6)

}
