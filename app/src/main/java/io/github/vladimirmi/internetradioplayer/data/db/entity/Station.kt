package io.github.vladimirmi.internetradioplayer.data.db.entity

import androidx.room.*
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
        @PrimaryKey val id: String,
        val name: String,
        val uri: String,
        val url: String?,
        val encoding: String?,
        val bitrate: String?,
        val sample: String?,
        val order: Int,
        @ColumnInfo(name = "group_id") val groupId: String
) {

    @Ignore
    constructor(name: String,
                uri: String,
                url: String?,
                encoding: String?,
                bitrate: String?,
                sample: String?)
            : this(UUID.randomUUID().toString(),
            name, uri, url, encoding, bitrate, sample,
            0, Group.DEFAULT_ID)

    companion object {
        fun nullObj() = Station("", "", null, null, null, null) //empty uri not valid (can't be)
    }

    @Ignore val specs: String

    init {
        val sb = StringBuilder()
        encoding?.let { sb.append(it.toUpperCase()) }
        sample?.let { sb.append(", ").append(it).append(" kHz") }
        bitrate?.let { sb.append(", ").append(it).append(" kbps") }
        specs = sb.toString()
    }

    fun isNull() = uri.isEmpty()
}
