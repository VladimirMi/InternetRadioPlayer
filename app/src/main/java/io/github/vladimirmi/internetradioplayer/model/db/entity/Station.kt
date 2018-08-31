package io.github.vladimirmi.internetradioplayer.model.db.entity

import android.arch.persistence.room.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(foreignKeys = [ForeignKey(entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["group_id"])])
class Station {

    @PrimaryKey(autoGenerate = true) var id = 0
    var name = ""
    var uri = ""
    var url = ""
    var bitrate = 0
    var sample = 0
    var order = 0
    @Embedded(prefix = "icon_") var icon: Icon? = null
    @ColumnInfo(name = "group_id") var groupId = 0
}
