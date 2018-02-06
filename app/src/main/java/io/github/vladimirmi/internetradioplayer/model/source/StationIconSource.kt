package io.github.vladimirmi.internetradioplayer.model.source

import android.content.Context
import android.net.Uri
import android.util.LruCache
import io.github.vladimirmi.internetradioplayer.extensions.clear
import io.github.vladimirmi.internetradioplayer.model.entity.icon.Icon
import io.github.vladimirmi.internetradioplayer.model.entity.icon.IconRes
import io.github.vladimirmi.internetradioplayer.model.manager.decode
import io.github.vladimirmi.internetradioplayer.model.manager.encode
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

class StationIconSource
@Inject constructor(context: Context) {

    private val FAVICON_BASE_URI = Uri.Builder().scheme("http")
            .authority("www.google.com")
            .path("s2/favicons").build()

    private val appDir = context.getExternalFilesDir(null)

    private val maxSize = (Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()
    private val bitmapCache = object : LruCache<String, Icon>(maxSize) {
        override fun sizeOf(key: String, value: Icon): Int {
            return value.bitmap.byteCount / 1024
        }
    }

    fun getIcon(path: String): Icon {
        synchronized(bitmapCache) {
            val cache = bitmapCache.get(path)
            if (cache != null) return cache
//        val icon = if (path.contains("http")) loadFromNet(path) else loadFromFile(path)
            val icon = loadFromFile(path)
            cache(icon)
            return icon
        }
    }

    fun getSavedIcon(path: String): Icon {
        return loadFromFile(path)
    }

    fun saveIcon(icon: Icon) {
        try {
            val file = File(appDir, "${icon.name}.png")
            if (file.exists()) file.clear()
            file.encode(icon)
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    fun removeIcon(name: String) {
        removeFromCache(name)
        File(appDir, "$name.png").delete()
    }

    fun cache(icon: Icon) {
        bitmapCache.put(icon.name, icon)
    }

    fun removeFromCache(name: String) {
        bitmapCache.remove(name)
    }

//    private fun loadFromNet(url: String): Icon {
//        val host = Uri.parse(url).host
//        val faviconUrl = FAVICON_BASE_URI.buildUpon()
//                .appendQueryParameter("domain", host)
//                .build().toURL() ?: return defaultIcon
//
//        val bitmap = faviconUrl.useConnection {
//            BitmapFactory.decodeStream(inputStream)
//        } ?: defaultIcon.bitmap
//        return Icon(url, bitmap)
//    }

    private fun loadFromFile(path: String): Icon {
        val file = File(appDir, "$path.png")
        if (!file.exists()) return IconRes(path)
        return file.decode()
    }
}