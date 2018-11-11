package io.github.vladimirmi.internetradioplayer.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(tableName = "station_genre_join",
        primaryKeys = ["stationId", "genreName"],
        indices = [Index(value = ["genreName"])],
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
