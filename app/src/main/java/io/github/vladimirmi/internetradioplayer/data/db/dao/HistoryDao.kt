package io.github.vladimirmi.internetradioplayer.data.db.dao

import androidx.room.*
import io.github.vladimirmi.internetradioplayer.data.db.entity.History
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistory(): Observable<List<History>>

    @Delete
    fun delete(history: History)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: History)
}