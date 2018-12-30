package io.github.vladimirmi.internetradioplayer.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

@Entity(primaryKeys = ["uri"])
class History(
        val timestamp: Long,
        @Embedded val station: Station
)