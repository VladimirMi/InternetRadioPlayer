package io.github.vladimirmi.internetradioplayer.data.service

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes

/**
 * Created by Vladimir Mikhalev 01.02.2018.
 */

val MediaMetadataCompat.artist: String? get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
val MediaMetadataCompat.title: String? get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)
val MediaMetadataCompat.album: String? get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
val MediaMetadataCompat.art: Bitmap? get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)

val EMPTY_METADATA: MediaMetadataCompat get() = MediaMetadataCompat.Builder().build()

fun MediaMetadataCompat.setArtistTitle(metadata: String): MediaMetadataCompat {

    val artistTitle = metadata.substringAfter("StreamTitle=", "")
            .substringBefore(';')
            .trim(' ', '\'')

    var (artist, title) = if (artistTitle.contains(" - ")) {
        artistTitle.split(" - ", limit = 2)
    } else {
        listOf("", artistTitle)
    }

    if (title.endsWith(']')) title = title.substringBeforeLast('[')
    if (title.isBlank() || artist.isBlank()) {
        title = Scopes.context.getString(R.string.metadata_not_available)
        artist = ""
    }

    return MediaMetadataCompat.Builder(this)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .build()
}

fun MediaMetadataCompat.setStation(station: Station, context: Context): MediaMetadataCompat {
    return MediaMetadataCompat.Builder(this)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, station.name)
//            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, station.icon.getBitmap(context, true))
            .build()
}

fun MediaMetadataCompat.clear(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder(this)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
            .build()
}

fun MediaMetadataCompat.isNotSupported(): Boolean {
    return title == Scopes.context.getString(R.string.metadata_not_available)
}

fun MediaMetadataCompat.isEmpty(): Boolean {
    return title.isNullOrBlank() && artist.isNullOrBlank()
}
