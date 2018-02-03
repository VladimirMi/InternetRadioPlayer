package io.github.vladimirmi.internetradioplayer.model.entity

import android.support.v4.media.MediaMetadataCompat

/**
 * Created by Vladimir Mikhalev 01.02.2018.
 */

class Metadata
private constructor() {

    var artist: String = unsupported
        private set
    var title: String = unsupported
        private set

    companion object {
        private const val unsupported = "unsupported"

        val UNSUPPORTED get() = Metadata()

        fun create(meta: String): Metadata {
            val keyValue = meta.split("=", limit = 2)
                    .map { it.trim(' ', '\'') }

            if (keyValue[0].isEmpty() || keyValue.size < 2 || keyValue[1].isEmpty()) {
                return UNSUPPORTED
            }
            if (keyValue[0] == "StreamTitle") {
                val value = keyValue[1]
                var (artist, title) = if (value.contains(" - ")) {
                    value.split(" - ", limit = 2)
                } else {
                    listOf("", value)
                }
                if (title.endsWith(']')) title = title.substringBeforeLast('[')

                return Metadata().apply {
                    this.artist = artist.trim()
                    this.title = title.trim()
                }
            }
            return UNSUPPORTED
        }

        fun create(meta: MediaMetadataCompat): Metadata {
            return Metadata().apply {
                artist = meta.description.subtitle.toString()
                title = meta.description.title.toString()
            }
        }
    }

    val isSupported get() = artist != unsupported && title != unsupported

    override fun toString(): String {
        return "$artist - $title"
    }

    fun toLogString(): String {
        return if (!isSupported) "Metadata(Unsupported)"
        else "Metadata(artist='$artist', title='$title')"
    }

    fun toMediaMetadata(): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build()
    }
}