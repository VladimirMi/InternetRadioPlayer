package io.github.vladimirmi.internetradioplayer.model.entity

import okhttp3.MediaType

/**
 * Created by Vladimir Mikhalev 10.11.2017.
 */


private val supportedAudioTypes = arrayOf("mpeg", "ogg", "opus", "aac", "aacp")
private val suppotedPlaylists = arrayOf("x-scpls", "mpegurl", "x-mpegurl", "x-mpegURL",
        "vnd.apple.mpegurl", "vnd.apple.mpegurl.audio", "x-pn-realaudio")

fun MediaType.isAudioStream() = supportedAudioTypes.contains(subtype())

fun MediaType.isPlaylistFile() = suppotedPlaylists.contains(subtype())

fun MediaType.isPlsFile() = subtype() == "x-scpl"
