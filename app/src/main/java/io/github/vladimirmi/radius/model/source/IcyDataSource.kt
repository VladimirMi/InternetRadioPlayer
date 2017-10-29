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
        val metaWindow = connection.getHeaderField("icy-metaint")
        if (metaWindow.isNullOrEmpty()) {
            Timber.e("stream does not support icy metadata")
        } else {
            val window = try {
                metaWindow.toInt()
            } catch (e: NumberFormatException) {
                Timber.e(e, "$metaWindow cannot be parsed"); 0
            }
            if (window > 0) {
                return IcyInputStream(connection.inputStream, window, playerCallback)
            }
        }
        return super.getInputStream(connection)
    }
}