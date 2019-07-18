package io.github.vladimirmi.internetradioplayer.data.service.player

import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import io.github.vladimirmi.internetradioplayer.data.service.PlayerCallback
import okhttp3.Call
import timber.log.Timber
import java.io.InputStream

/**
 * Created by Vladimir Mikhalev 23.01.2019.
 */

private const val REQUEST_ICY_METADATA_HEADER = "Icy-Metadata"
private const val RESPONSE_ICY_METAINT_HEADER = "icy-metaint"

class IcyHttpDataSource(callFactory: Call.Factory,
                        userAgent: String,
                        private val playerCallback: PlayerCallback? = null)
    : OkHttpDataSource(callFactory, userAgent, null) {

    init {
        setRequestProperty(REQUEST_ICY_METADATA_HEADER, "1")
    }

    override fun open(dataSpec: DataSpec): Long {
        val bytesToRead = super.open(dataSpec)
        var metadataWindow = 0

        try {
            metadataWindow = responseHeaders[RESPONSE_ICY_METAINT_HEADER]?.get(0)?.toInt() ?: 0
            if (metadataWindow > 0) {
                val field = javaClass.superclass?.getDeclaredField("responseByteStream")
                field!!.isAccessible = true
                val inS = field.get(this) as InputStream
                field.set(this, IcyInputStream(inS, metadataWindow, playerCallback))
            }
        } catch (ex: Exception) {
        }

        if (metadataWindow == 0) {
            playerCallback?.setMetadata("")
            Timber.d("stream does not support icy metadata")
        }

        return bytesToRead
    }
}