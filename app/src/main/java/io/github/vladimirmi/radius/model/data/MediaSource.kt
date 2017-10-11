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

class MediaSource
@Inject constructor(private val context: Context, preferences: Preferences) {

    val mediaList = ArrayList<Media>()

    init {
        if (preferences.firstRun) {
            copyFilesFromAssets()
            preferences.firstRun = false
        }
        parseFileTree()
    }

    private fun copyFilesFromAssets() {
        context.assets.list("")
                .filter { it.endsWith(".pls") }
                .forEach { copyFile(it, getPlaylistDir().path) }
    }

    private fun getPlaylistDir(): File {
        val appDir = Environment.getExternalStoragePublicDirectory("Radius")
        val playlistDir = File(appDir, "Playlist")
        if (!playlistDir.mkdirs() && (!playlistDir.exists() || !playlistDir.isDirectory)) {
            throw IllegalStateException("Can not create playlist folder")
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

    private fun parseFileTree() {
        val treeWalk = getPlaylistDir().walkTopDown()
        var groupDirName: String? = null
        treeWalk.forEach {
            if (it.isDirectory) {
                groupDirName = it.name
            } else {
                val media = fromFile(it)!!
                media.genres.add(groupDirName ?: "NONE")
                mediaList.add(media)
            }
        }
    }

    private fun fromFile(file: File): Media? {
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