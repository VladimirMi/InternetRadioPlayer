package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.os.Environment
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.manager.clear
import io.github.vladimirmi.radius.model.manager.parsePls
import io.github.vladimirmi.radius.model.manager.update
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaSource
@Inject constructor(private val context: Context,
                    private val preferences: Preferences) {
    private lateinit var appDir: File

    fun getMediaList(): List<Media> {
        copyFilesFromAssets()
        val mediaList = ArrayList<Media>()
        val treeWalk = getAppDir().walkTopDown()
        treeWalk.forEach {
            if (!it.isDirectory) {
                fromFile(it)?.let { mediaList.add(it) }
            }
        }
        return mediaList
    }

    fun update(media: Media) {
        File(media.playlistPath).update(media)
    }

    fun clear(media: Media) {
        File(media.playlistPath).clear()
    }

    private fun copyFilesFromAssets() {
        appDir = getAppDir()
        if (preferences.firstRun) {
            context.assets.list("")
                    .filter { it.endsWith(".pls") }
                    .forEach { copyFile(it, appDir.path) }
            preferences.firstRun = false
        }
    }

    private fun getAppDir(): File {
        val appDir = Environment.getExternalStoragePublicDirectory("Radius")
        if (!appDir.mkdirs() && (!appDir.exists() || !appDir.isDirectory)) {
            throw IllegalStateException("Can not create playlist folder")
        }
        return appDir
    }

    private fun copyFile(filePath: String, destination: String) {
        Timber.e("copyFile: $filePath")
        context.assets.open(filePath).use { inS ->
            FileOutputStream(File(destination, filePath)).use { outS ->
                inS.copyTo(outS)
            }
        }
    }

    private fun fromFile(file: File): Media? {
        return when (file.extension) {
            "pls" -> file.parsePls()
            else -> null
        }
    }
}
