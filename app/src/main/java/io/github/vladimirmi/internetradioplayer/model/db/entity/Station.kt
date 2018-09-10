package io.github.vladimirmi.internetradioplayer.model.db.entity

import android.arch.persistence.room.*
import io.github.vladimirmi.internetradioplayer.extensions.randomIcon
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(foreignKeys = [ForeignKey(entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["group_id"])],
        indices = [Index(value = ["name"], unique = true)])
class Station {

    @PrimaryKey var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var uri: String = ""
    var url: String? = null
    var bitrate: Int? = null
    var sample: Int? = null
    var order: Int = 0
    @Embedded(prefix = "icon_") var icon: Icon = randomIcon()
    @ColumnInfo(name = "group_id") var groupId: String = ""

    @Ignore var genres: List<String> = listOf()
    @Ignore var group: String = ""
}
