package io.github.vladimirmi.internetradioplayer.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

@Entity()
class EqualizerPreset(@PrimaryKey val name: String,
                      val bands: String,
                      val bass: String,
                      val virtualizer: String)