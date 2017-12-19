package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.LruCache
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.extensions.getBitmap
import io.github.vladimirmi.radius.extensions.toURL
import io.github.vladimirmi.radius.extensions.useConnection
import io.github.vladimirmi.radius.model.entity.Station
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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

    private val appDir = context.getExternalFilesDir(null)

    private val defaultIcon: Bitmap
        get() = ContextCompat.getDrawable(context, R.drawable.ic_radius).getBitmap()

    private val maxSize = (Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()
    private val bitmapCache = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun getIcon(path: String, cacheKey: String? = null): Bitmap {
        val cache = bitmapCache.get(cacheKey ?: path)
        if (cache != null) return cache
        val bitmap = if (path.contains("http")) loadFromNet(path) else loadFromFile(path)
        bitmapCache.put(cacheKey ?: path, bitmap)
        return bitmap
    }

    fun saveIcon(fileName: String, bitmap: Bitmap) {
        try {
            val file = File(appDir, "$fileName.png")
            if (file.exists()) file.clear()
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: IOException) {
        }
    }

    fun removeIcon(station: Station) {
        bitmapCache.remove(station.title)
        File(appDir, "${station.title}.png").delete()
    }

    fun cacheIcon(key: String, value: Bitmap) {
        bitmapCache.put(key, value)
    }

    private fun loadFromNet(url: String): Bitmap {
        val host = Uri.parse(url).host
        val faviconUrl = FAVICON_BASE_URI.buildUpon()
                .appendQueryParameter("domain", host)
                .build().toURL() ?: return defaultIcon

        Timber.e("loadFromNet: $faviconUrl")
        return faviconUrl.useConnection {
            BitmapFactory.decodeStream(inputStream)
        } ?: defaultIcon
    }

    private fun loadFromFile(path: String): Bitmap {
        val file = File(appDir, "$path.png")
        if (!file.exists()) return defaultIcon
        Timber.e("loadFromFile: ${file.path}")
        return FileInputStream(file).use {
            BitmapFactory.decodeStream(it)
        }
    }
}