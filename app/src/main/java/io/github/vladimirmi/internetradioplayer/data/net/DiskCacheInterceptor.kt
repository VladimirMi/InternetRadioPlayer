package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.utils.DiskCacheManager
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.internal.http.RealResponseBody
import okhttp3.internal.io.FileSystem
import okio.Okio
import timber.log.Timber
import java.io.FileWriter
import java.io.IOException

/**
 * Created by Vladimir Mikhalev 31.03.2019.
 */

class DiskCacheInterceptor(private val cacheManager: DiskCacheManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        cacheManager.cleanExpiredCache()

        val cacheFile = cacheManager.getCache(chain.request().url())

        if (!cacheFile.exists() || cacheFile.length() == 0L) {
            val response = chain.proceed(chain.request())
            if (response.isSuccessful) {
                try {
                    Timber.d("Write ${cacheFile.name}")
                    FileWriter(cacheFile).use { it.write(response.body()!!.string()) }
                } catch (e: IOException) {
                    Timber.e(e)
                    return response
                }
            } else {
                return response
            }
        } else {
            Timber.d("Return ${cacheFile.name}")
        }

        val cacheSource = Okio.buffer(FileSystem.SYSTEM.source(cacheFile))

        return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(RealResponseBody("text", cacheFile.length(), cacheSource))
                .build()
    }
}