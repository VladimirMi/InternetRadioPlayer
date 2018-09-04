package io.github.vladimirmi.internetradioplayer.model.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Entity(indices = [Index(value = ["name"], unique = true)])
class Group() {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var expanded: Boolean = true
    var order: Int = 0
    @Ignore var items: MutableList<Station> = arrayListOf()

    constructor(name: String) : this() {
        this.name = name
    }

    companion object {
        const val DEFAULT_ID = "default_id"

        fun default(): Group {
            return Group().apply {
                id = DEFAULT_ID
                name = Scopes.context.getString(R.string.default_group)
            }
        }
    }

    fun isDefault() = id == DEFAULT_ID
}
