package io.github.vladimirmi.internetradioplayer.domain.model

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

