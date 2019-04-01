package io.github.vladimirmi.internetradioplayer.data.utils

import android.content.Context
import io.github.vladimirmi.internetradioplayer.data.net.CoverArtService
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import okhttp3.HttpUrl
import timber.log.Timber
import java.io.File

/**
 * Created by Vladimir Mikhalev 29.03.2019.
 */

class DiskCacheManager(context: Context) {

    companion object {
        private const val PREFIX = "cache"
        private const val EXTENSION = "json"

        private const val MINUTE = 60
        private const val HOUR = MINUTE * 60
        private const val DAY = HOUR * 24

        val EXPIRATION_MAP = mapOf(
                UberStationsService.PRESEARCH_ENDPOINT to HOUR,
                UberStationsService.STATIONS_ENDPOINT to MINUTE,
                UberStationsService.STATION_ENDPOINT to 3 * DAY,
                UberStationsService.TOPSONGS_ENDPOINT to MINUTE,
                UberStationsService.TALKS_ENDPOINT to HOUR,
                UberStationsService.TALK_ENDPOINT to DAY,
                CoverArtService.RECORDING_ENDPOINT to DAY
        )
    }

    private val cacheDir = context.cacheDir

    fun getCache(url: HttpUrl): File {
        val pathNamePart = url.encodedPath().substringAfter('/')
        val expirationDurationSec = EXPIRATION_MAP[pathNamePart] ?: 0
        val expirationTime = System.currentTimeMillis() + expirationDurationSec * 1000

        val query = (0 until url.querySize())
                .map { url.queryParameterValue(it) }
                .toTypedArray()
        val urlNamePart = buildUrlNamePart(pathNamePart, *query)

        return findCache(urlNamePart) ?: createNewCache(urlNamePart, expirationTime)
    }

    fun cleanExpiredCache() {
        val files = cacheDir.listFiles { _, name -> name.startsWith(PREFIX) }
        for (file in files) {
            if (isCacheExpired(file)) {
                deleteFile(file)
            }
        }
    }

    fun cleanCache(vararg query: String) {
        val partName = buildUrlNamePart(*query)
        val files = cacheDir.listFiles { _, name -> name.contains(partName) }
        for (file in files) {
            deleteFile(file)
        }
    }

    private fun buildUrlNamePart(vararg query: String): String {
        return query.joinToString("_", transform = { it.replace("/", "") })
    }

    private fun findCache(namePart: String): File? {
        return cacheDir.listFiles { _, name -> name.contains(namePart) }
                .firstOrNull()
    }

    private fun createNewCache(namePart: String, expirationTime: Long): File {
        val fileName = String.format("%s_%s_%s.%s", PREFIX, namePart, expirationTime, EXTENSION)
        return File(cacheDir, fileName)
    }

    private fun isCacheExpired(cache: File): Boolean {
        val begin = cache.name.lastIndexOf('_') + 1
        val end = cache.name.lastIndexOf('.')
        val expirationTime = cache.name.substring(begin, end)
        return expirationTime.toLong() <= System.currentTimeMillis()
    }

    private fun deleteFile(file: File) {
        if (file.delete()) {
            Timber.d("Delete %s", file.name)
        } else {
            Timber.w("Can't delete %s", file.name)
        }
    }
}