package io.github.vladimirmi.internetradioplayer.model.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Group(@PrimaryKey
                 val id: String,
                 val name: String,
                 val expanded: Boolean,
                 val order: Int) {

    @Ignore var stations: MutableList<Station> = arrayListOf()

    @Ignore
    constructor(id: String, name: String, order: Int) : this(id, name, true, order)

    companion object {
        const val DEFAULT_ID = "default_id"
        const val DEFAULT_NAME = "default_name"
    }

    fun isDefault() = id == DEFAULT_ID
}
