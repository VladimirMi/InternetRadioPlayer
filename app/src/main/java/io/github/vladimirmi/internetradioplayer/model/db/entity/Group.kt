package io.github.vladimirmi.internetradioplayer.model.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity
class Group {

    @PrimaryKey(autoGenerate = true) var id = 0
    var name = ""
    var expanded = true
    var order = 0
    @Ignore var items: MutableList<Station> = arrayListOf()
}
