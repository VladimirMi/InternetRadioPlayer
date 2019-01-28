package io.github.vladimirmi.internetradioplayer.domain.model

import java.util.*

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

sealed class Suggestion(val value: String) {

    class Recent(value: String) : Suggestion(value)
    class Regular(value: String) : Suggestion(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Suggestion

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Suggestion(value='$value')"
    }
}

class SuggestionList : AbstractList<Suggestion>() {

    var recent = emptyList<Suggestion>()
    var regular = emptyList<Suggestion>()

    override val size: Int
        get() = recent.size + regular.size

    override fun get(index: Int): Suggestion {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("index=$index; size=$size")

        return if (index < recent.size) {
            recent[index]
        } else {
            regular[index - recent.size]
        }
    }

    fun copy() = SuggestionList().apply {
        recent = ArrayList(this@SuggestionList.recent)
        regular = ArrayList(this@SuggestionList.regular)
    }
}

