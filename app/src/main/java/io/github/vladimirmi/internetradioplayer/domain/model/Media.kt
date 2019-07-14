package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

interface Media {

    val id: String
    val name: String
    val uri: String
    val group: String
    val specs: String?
    val description: String?
    val genre: String?
    val language: String?
    val location: String?
    val url: String?

    companion object {
        fun nullObj() = object : Media {
            override val id = ""
            override val name = ""
            override val uri = ""
            override val group = ""
            override val specs = null
            override val description = null
            override val genre = null
            override val language = null
            override val location = null
            override val url = null
        }
    }

    fun isNull() = id.isEmpty()

    val languageString: String
        get() {
            val sb = StringBuilder()
            language?.let { sb.append(language) }
            location?.let { sb.append(", ").append(location) }
            return sb.trim(' ', ',').toString()
        }
}