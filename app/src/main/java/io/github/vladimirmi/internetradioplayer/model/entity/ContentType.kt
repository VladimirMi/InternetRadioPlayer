package io.github.vladimirmi.internetradioplayer.model.entity

/**
 * Created by Vladimir Mikhalev 10.11.2017.
 */

enum class ContentType(val types: Array<String>) {

    MPEG(arrayOf("audio/mpeg")),
    OGG(arrayOf("audio/ogg", "application/ogg", "audio/opus")),
    ACC(arrayOf("audio/aac", "audio/aacp")),

    PLS(arrayOf("audio/x-scpls")),
    M3U(arrayOf("audio/mpegurl", "application/x-mpegurl", "application/x-mpegURL",
            "audio/x-mpegurl", "application/x-mpegurl")),
    HLS(arrayOf("application/vnd.apple.mpegurl", "application/vnd.apple.mpegurl.audio")),
    RAM(arrayOf("audio/x-pn-realaudio"));

    companion object {
        fun isPlaylist(type: String): Boolean {
            return PLS.types.contains(type) ||
                    M3U.types.contains(type) ||
                    HLS.types.contains(type) ||
                    RAM.types.contains(type)
        }

        fun isAudio(type: String): Boolean {
            return MPEG.types.contains(type) ||
                    OGG.types.contains(type) ||
                    ACC.types.contains(type)
        }

        private val MAP: Map<String, ContentType> by lazy {
            val map = HashMap<String, ContentType>()
            values().forEach { contentTypes ->
                contentTypes.types.forEach {
                    map[it] = contentTypes
                }
            }
            map
        }

        fun fromString(type: String): ContentType? = MAP[type]
    }
}
