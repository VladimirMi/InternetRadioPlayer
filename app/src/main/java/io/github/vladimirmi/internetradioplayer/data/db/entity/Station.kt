package io.github.vladimirmi.internetradioplayer.data.db.entity

import android.arch.persistence.room.*
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

//todo create group_id index
@Entity(foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["name"], unique = true), Index(value = ["group_id"])])

data class Station(
        @PrimaryKey val id: String,
        val name: String,
        val uri: String,
        val url: String?,
        val bitrate: Int?,
        val sample: Int?,
        val order: Int,
        @Embedded(prefix = "icon_") val icon: Icon,
        @ColumnInfo(name = "group_id") val groupId: String
) {

    @Ignore var genres: List<String> = listOf()
    @Ignore var groupName: String = Group.DEFAULT_NAME

    @Ignore constructor(name: String,
                        uri: String,
                        url: String?,
                        bitrate: Int?,
                        sample: Int?)
            : this(UUID.randomUUID().toString(), name, uri, url, bitrate, sample,
            0, Icon.randomIcon(), Group.DEFAULT_ID)

    companion object {
        fun nullObj() = Station("", "", "", 0, 0) //empty uri not valid (can't be)
    }

    fun isNull() = uri.isEmpty()
}
