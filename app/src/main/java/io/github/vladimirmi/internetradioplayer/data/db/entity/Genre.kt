package io.github.vladimirmi.internetradioplayer.data.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity
class Genre(@PrimaryKey val name: String)
