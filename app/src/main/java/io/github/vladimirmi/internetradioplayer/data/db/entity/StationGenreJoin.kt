package io.github.vladimirmi.internetradioplayer.data.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(tableName = "station_genre_join",
        primaryKeys = ["stationId", "genreName"],
        foreignKeys = [
            ForeignKey(entity = Station::class,
                    parentColumns = ["id"],
                    childColumns = ["stationId"],
                    onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Genre::class,
                    parentColumns = ["name"],
                    childColumns = ["genreName"],
                    onDelete = ForeignKey.RESTRICT)
        ])
class StationGenreJoin(var stationId: String, var genreName: String)
