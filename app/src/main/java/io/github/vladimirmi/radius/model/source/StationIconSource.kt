package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.LruCache
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.extensions.getBitmap
import io.github.vladimirmi.radius.extensions.toURL
import io.github.vladimirmi.radius.extensions.useConnection
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.manager.decode
import io.github.vladimirmi.radius.model.manager.encode
import timber.log.Timber
import java.io.File
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

    val defaultIcon: Icon by lazy {
        val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_station_1)).mutate()
        val accentColor = ContextCompat.getColor(context, R.color.accentColor)
        DrawableCompat.setTint(drawable, accentColor)
        Icon("default", drawable.getBitmap(), foregroundColor = accentColor)
    }

    private val maxSize = (Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()
    private val bitmapCache = object : LruCache<String, Icon>(maxSize) {
        override fun sizeOf(key: String, value: Icon): Int {
            return value.bitmap.byteCount / 1024
        }
    }

    fun getIcon(path: String): Icon {
        val cache = bitmapCache.get(path)
        if (cache != null) return cache
        val icon = if (path.contains("http")) loadFromNet(path) else loadFromFile(path)
        if (icon != defaultIcon) cacheIcon(icon)
        return icon
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
        bitmapCache.remove(name)
        File(appDir, "$name.png").delete()
    }

    fun cacheIcon(icon: Icon) {
        bitmapCache.put(icon.name, icon)
    }

    private fun loadFromNet(url: String): Icon {
        val host = Uri.parse(url).host
        val faviconUrl = FAVICON_BASE_URI.buildUpon()
                .appendQueryParameter("domain", host)
                .build().toURL() ?: return defaultIcon

        val bitmap = faviconUrl.useConnection {
            BitmapFactory.decodeStream(inputStream)
        } ?: defaultIcon.bitmap
        return Icon(url, bitmap)
    }

    private fun loadFromFile(path: String): Icon {
        val file = File(appDir, "$path.png")
        if (!file.exists()) return defaultIcon
        return file.decode()
    }
}