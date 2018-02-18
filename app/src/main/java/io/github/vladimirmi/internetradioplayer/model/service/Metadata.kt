package io.github.vladimirmi.internetradioplayer.model.service

import android.support.v4.media.MediaMetadataCompat

/**
 * Created by Vladimir Mikhalev 01.02.2018.
 */

class Metadata
private constructor(val artist: String = unsupported, val title: String = unsupported) {

    companion object {
        private const val unsupported = "unsupported"

        val UNSUPPORTED get() = Metadata()

        fun create(meta: String): Metadata {
            val artistTitle = meta.substringAfter("StreamTitle=", unsupported)
                    .substringBefore(';')
                    .trim(' ', '\'')

            var (artist, title) = if (artistTitle.contains(" - ")) {
                artistTitle.split(" - ", limit = 2)
            } else {
                listOf("", artistTitle)
            }

            if (title.isEmpty()) return UNSUPPORTED
            if (title.endsWith(']')) title = title.substringBeforeLast('[')

            return Metadata(artist.trim(), title.trim())
        }

        fun create(meta: MediaMetadataCompat): Metadata {
            return Metadata(
                    artist = meta.description.subtitle.toString(),
                    title = meta.description.title.toString()
            )
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        if (artist != other.artist) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = artist.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }
}
