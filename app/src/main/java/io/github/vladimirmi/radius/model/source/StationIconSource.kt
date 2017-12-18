package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import io.github.vladimirmi.radius.extensions.toURL
import io.github.vladimirmi.radius.model.entity.Station
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

class StationIconSource
@Inject constructor(private val context: Context) {

    @Suppress("PrivatePropertyName")
    private val FAVICON_BASE_URI = Uri.Builder().scheme("http")
            .authority("www.google.com")
            .path("s2/favicons").build()


    private val maxSize = (Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()
    private val bitmapCache = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun getIcon(path: String): Bitmap {
        val bitmap = if (path.contains("http")) loadFromNet(path) else loadFromFile(path)
        bitmapCache.put(path, bitmap)
        return bitmap
    }

    fun saveIcon(station: Station, bitmap: Bitmap) {
        bitmapCache.put(station.title, bitmap)
        FileOutputStream(File(context.filesDir, "${station.title}.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    fun removeIcon(station: Station) {
        bitmapCache.remove(station.title)
        File(context.filesDir, "${station.title}.png").delete()
    }

    private fun loadFromNet(url: String): Bitmap {
        val faviconUri = FAVICON_BASE_URI.buildUpon()
                .appendQueryParameter("domain_url", url).build()
        Timber.e("loadFromNet: $faviconUri")
        val inputStream = faviconUri.toURL()?.openStream()
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun loadFromFile(path: String): Bitmap {
        val inputStream = FileInputStream(File(context.filesDir, "$path.png"))
        return BitmapFactory.decodeStream(inputStream)
    }
}