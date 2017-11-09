package io.github.vladimirmi.radius.model.source

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.vladimirmi.radius.extensions.getContentType
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.manager.clear
import io.github.vladimirmi.radius.model.manager.parsePls
import io.github.vladimirmi.radius.model.manager.savePls
import kotlinx.coroutines.experimental.launch
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
    private val appDir: File by lazy { initAppDir() }

    fun getMediaList(): ArrayList<Media> {
        copyFilesFromAssets()
        val mediaList = ArrayList<Media>()
        val treeWalk = initAppDir().walkTopDown()
        treeWalk.forEach {
            if (!it.isDirectory) {
                fromFile(it)?.let { mediaList.add(it) }
            }
        }
        return mediaList
    }

    fun save(media: Media) {
        media.savePls()
    }

    fun clear(media: Media) {
        File(media.path).clear()
    }

    fun download(uri: Uri) {
        launch {
            Timber.e("download: type ${uri.getContentType()}")
        }
        File(appDir, uri.lastPathSegment).delete()
        val request = DownloadManager.Request(uri)
        request.setDescription("Some descrition")
                .setTitle("Some title")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.withAppendedPath(Uri.fromFile(appDir), uri.lastPathSegment))

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
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

    private fun fromFile(file: File): Media? {
        return when (file.extension) {
            "pls" -> file.parsePls()
            else -> null
        }
    }

//    private fun fromUri(uri: Uri): Media {
//
//    }
}
