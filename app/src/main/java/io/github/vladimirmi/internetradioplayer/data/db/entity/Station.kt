package io.github.vladimirmi.internetradioplayer.data.db.entity

import androidx.room.*
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["uri"], unique = true), Index(value = ["group_id"])])

data class Station(
        @PrimaryKey
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        override val uri: String,
        override val remoteId: String,
        val encoding: String? = null,
        val bitrate: String? = null,
        val sample: String? = null,
        val order: Int = 0,
        @ColumnInfo(name = "group_id")
        val groupId: String = Group.DEFAULT_ID,
        val equalizerPreset: String? = null,
        override val description: String? = null,
        override val genre: String? = null,
        override val language: String? = null,
        override val location: String? = null,
        override val url: String? = null
) : Media {

    @Ignore override val specs: String
    @Ignore override var group: String = Group.DEFAULT_NAME

    init {
        val sb = StringBuilder()
        encoding?.let { sb.append(it.toUpperCase()) }
        sample?.let { sb.append(", ").append(it).append(" Hz") }
        bitrate?.let { sb.append(", ").append(it).append(" kbps") }
        specs = sb.trim(' ', ',').toString()
    }

    @Ignore var isFavorite = false
}
