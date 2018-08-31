package io.github.vladimirmi.internetradioplayer.model.manager

import android.content.Context
import io.github.vladimirmi.internetradioplayer.extensions.Preference
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class Preferences
@Inject constructor(context: Context) {

    var currentPos: Int by Preference(context, "CURRENT_POSITION", 0)

    var hidedGroups: Set<String> by Preference(context, "HIDED_GROUPS", emptySet())
}
