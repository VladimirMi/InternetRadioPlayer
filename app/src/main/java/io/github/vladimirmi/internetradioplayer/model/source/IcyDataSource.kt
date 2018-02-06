package io.github.vladimirmi.internetradioplayer.model.source

import com.google.android.exoplayer2.util.Predicate
import io.github.vladimirmi.internetradioplayer.com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import io.github.vladimirmi.internetradioplayer.model.service.Metadata
import io.github.vladimirmi.internetradioplayer.model.service.PlayerCallback
import timber.log.Timber
import java.io.InputStream
import java.net.HttpURLConnection

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyDataSource(userAgent: String,
                    contentTypePredicate: Predicate<String>?,
                    private val playerCallback: PlayerCallback)
    : DefaultHttpDataSource(userAgent, contentTypePredicate) {

    init {
        setRequestProperty("Icy-Metadata", "1")
    }

    override fun getInputStream(connection: HttpURLConnection): InputStream {
        Timber.d("getInputStream: $responseHeaders")
        val metaWindow = connection.getHeaderFieldInt("icy-metaint", 0)

        return if (metaWindow > 0) {
            IcyInputStream(connection.inputStream, metaWindow, playerCallback)
        } else {
            Timber.d("stream does not support icy metadata")
            playerCallback.onMetadata(Metadata.UNSUPPORTED)
            super.getInputStream(connection)
        }
    }
}