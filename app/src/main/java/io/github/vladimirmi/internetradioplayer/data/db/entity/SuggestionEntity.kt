package io.github.vladimirmi.internetradioplayer.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity
class SuggestionEntity(@PrimaryKey val value: String,
                       val lastModified: Long)
