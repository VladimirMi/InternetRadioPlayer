package io.github.vladimirmi.radius.model.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.vladimirmi.radius.model.entity.Media
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaManager
@Inject constructor(private val context: Context, private val preferences: Preferences) {

    init {
        if (preferences.firstRun) {
            copyFilesToSdCard()
            preferences.firstRun = false
        }
    }

    private fun copyFilesToSdCard() {
        context.assets.list("")
                .filter { it.endsWith(".pls") }
                .forEach { copyFile(it, getPlaylistDir()!!.path) }
    }

    private fun getPlaylistDir(): File? {
        val appDir = Environment.getExternalStoragePublicDirectory("Radius")
        val playlistDir = File(appDir, "Playlist")
        if (!playlistDir.mkdirs() && (!playlistDir.exists() || !playlistDir.isDirectory)) {
            return null
        }
        return playlistDir
    }

    private fun copyFile(filePath: String, destination: String) {
        Timber.e("copyFile: $filePath")
        context.assets.open(filePath).use { inS ->
            FileOutputStream(File(destination, filePath)).use { outS ->
                inS.copyTo(outS)
            }
        }
    }

    fun fromFile(file: File): Media? {
        var name: String? = null
        var uri: Uri? = null
        file.useLines { line ->
            line.forEach {
                when {
                    it.startsWith("Title1=") -> name = it.substring(7).trim() // pls
                    it.startsWith("File1=http") -> uri = Uri.parse(it.substring(6).trim()) // pls
                    it.startsWith("#EXTINF:-1,") -> name = it.substring(11).trim() // m3u
                    it.startsWith("http") -> uri = Uri.parse(it.trim()) // m3u
                }
            }
        }
        if (uri == null) {
            Timber.e("fromFile: Unable to parse file: ${file.name}")
            return null
        }
        if (name == null) {
            name = file.name.substringBeforeLast(".")
        }
        return Media(name!!, uri!!)
    }

}