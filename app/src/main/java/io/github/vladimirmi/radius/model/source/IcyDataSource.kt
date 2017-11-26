package io.github.vladimirmi.radius.model.source

import com.google.android.exoplayer2.util.Predicate
import io.github.vladimirmi.radius.com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import io.github.vladimirmi.radius.model.service.PlayerCallback
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
        val metaWindow = connection.getHeaderFieldInt("icy-metaint", 0)

        return if (metaWindow > 0) {
            IcyInputStream(connection.inputStream, metaWindow, playerCallback)
        } else {
            Timber.e("stream does not support icy metadata")
            super.getInputStream(connection)
        }
    }
}