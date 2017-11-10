package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.extensions.toUrl
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.manager.parsePls
import io.github.vladimirmi.radius.model.manager.saveToPls
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaSource
@Inject constructor(private val context: Context,
                    private val preferences: Preferences) {

    private val appDir: File by lazy { initAppDir() }

    fun getMediaList(): ArrayList<Media> {
        copyFilesFromAssets()
        val mediaList = ArrayList<Media>()
        val treeWalk = initAppDir().walkTopDown()
        treeWalk.forEach {
            if (!it.isDirectory) {
                Timber.e("file ${it.path}")
                fromFile(it)?.let { mediaList.add(it) }
            }
        }
        return mediaList
    }

    fun save(media: Media) {
        media.saveToPls()
    }

    fun clear(media: Media) {
        File(media.path).clear()
    }

    fun fromUri(uri: Uri, cb: (Media?) -> Unit) {
        when {
            uri.scheme.startsWith("http") -> fromNet(uri.toUrl() ?: return, cb)
            uri.scheme.startsWith("file") -> fromFile(File(URI.create(uri.toString())), cb)
        }
    }

    private fun copyFilesFromAssets() {
        if (preferences.firstRun) {
            context.assets.list("")
                    .filter { it.endsWith(".pls") }
                    .forEach { copyFile(it, appDir.path) }
            preferences.firstRun = false
        }
    }

    private fun initAppDir(): File {
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

    private fun fromNet(url: URL, cb: (Media?) -> Unit) {
        val file = File(appDir, "test")
        launch {
            val media = url.openStream().bufferedReader().readLines().parsePls(file)
            media?.let { save(it) }
            cb(media)
        }
    }

    private fun fromFile(file: File): Media? {
        return when (file.extension) {
            "pls" -> file.parsePls()
            else -> null
        }
    }

    private fun fromFile(file: File, cb: (Media?) -> Unit) {
        cb(fromFile(file))
    }
}
