package io.github.vladimirmi.internetradioplayer.data.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import io.github.vladimirmi.internetradioplayer.R

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

    fun getViewName(context: Context): String {
        return if (name == DEFAULT_NAME) context.getString(R.string.default_group)
        else name
    }
}
