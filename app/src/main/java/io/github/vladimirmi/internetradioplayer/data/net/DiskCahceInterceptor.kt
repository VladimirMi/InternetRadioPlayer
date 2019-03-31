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

/**
 * Created by Vladimir Mikhalev 31.03.2019.
 */

class DiskCahceInterceptor(private val cacheManager: DiskCacheManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        cacheManager.cleanExpiredCache()

        val cacheFile = cacheManager.getCache(chain.request().url())

        if (!cacheFile.exists() || cacheFile.length() == 0L) {
            val response = chain.proceed(chain.request())
            if (response.isSuccessful) {
                Timber.d("Write ${cacheFile.name}")
                val fileWriter = FileWriter(cacheFile)

                fileWriter.write(response.body()!!.string())
                fileWriter.close()
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