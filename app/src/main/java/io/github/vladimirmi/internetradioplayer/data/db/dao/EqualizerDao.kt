package io.github.vladimirmi.internetradioplayer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.vladimirmi.internetradioplayer.data.db.entity.EqualizerPresetEntity
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

@Dao
interface EqualizerDao {

    @Query("SELECT * FROM equalizerpresetentity")
    fun getPresets(): Single<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(preset: EqualizerPresetEntity)
}