package io.github.vladimirmi.internetradioplayer.presentation.station

import android.content.Context
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group

/**
 * Created by Vladimir Mikhalev 12.09.2018.
 */

class StationInfo(val stationName: String, group: String, val genres: List<String>, context: Context) {

    val groupName = if (group == context.getString(R.string.default_group) || group.isBlank()) Group.DEFAULT_NAME else group
}
