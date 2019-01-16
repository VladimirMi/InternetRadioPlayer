package io.github.vladimirmi.internetradioplayer.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import io.github.vladimirmi.internetradioplayer.data.db.entity.EqualizerPreset
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

@Dao
interface EqualizerDao {

    @Query("SELECT * FROM equalizerpreset")
    fun getPresets(): Single<List<EqualizerPreset>>
}