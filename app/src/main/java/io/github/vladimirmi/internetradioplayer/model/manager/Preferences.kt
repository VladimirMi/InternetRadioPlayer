package io.github.vladimirmi.internetradioplayer.model.manager

import android.content.Context
import io.github.vladimirmi.internetradioplayer.extensions.Preference
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class Preferences
@Inject constructor(context: Context) {

    var currentStationId: String by Preference(context, "CURRENT_STATION_ID", "")

}
