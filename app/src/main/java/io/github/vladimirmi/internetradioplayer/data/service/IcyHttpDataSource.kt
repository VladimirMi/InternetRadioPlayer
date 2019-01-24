package io.github.vladimirmi.internetradioplayer.data.service

import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import okhttp3.Call
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 23.01.2019.
 */

private const val REQUEST_ICY_METADATA_HEADER = "Icy-Metadata"
private const val RESPONSE_ICY_METAINT_HEADER = "icy-metaint"

class IcyHttpDataSource(callFactory: Call.Factory,
                        userAgent: String,
                        private val playerCallback: PlayerCallback)
    : OkHttpDataSource(callFactory, userAgent, null) {

    private var metadataWindow = 0
    private var bytesBeforeMetadata = 0
    private var metadataBuffer = ByteArray(128)

    init {
        setRequestProperty(REQUEST_ICY_METADATA_HEADER, "1")
    }

    override fun open(dataSpec: DataSpec): Long {
        val bytesToRead = super.open(dataSpec)

        try {
            metadataWindow = responseHeaders[RESPONSE_ICY_METAINT_HEADER]?.get(0)?.toInt() ?: 0
            bytesBeforeMetadata = metadataWindow
        } catch (ex: NumberFormatException) {
        }
        if (metadataWindow == 0) {
            playerCallback.onMetadata("")
            Timber.d("stream does not support icy metadata")
        }

        return bytesToRead
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return if (metadataWindow == 0) {
            super.read(buffer, offset, readLength)
        } else {
            val read = if (bytesBeforeMetadata < readLength) bytesBeforeMetadata else readLength
            val bytes = super.read(buffer, offset, read)
            bytesBeforeMetadata -= bytes
            if (bytesBeforeMetadata == 0) readMetadata()
            return bytes
        }
    }

    private fun readMetadata() {
        bytesBeforeMetadata = metadataWindow
        val sizeBuffer = ByteArray(1)
        super.read(sizeBuffer, 0, 1)
        val size = sizeBuffer[0] * 16
        if (size < 1) return
        if (size > metadataBuffer.size) {
            metadataBuffer = ByteArray(size)
        }
        ensureFill(metadataBuffer, 0, size)
        val actualSize = metadataBuffer.indexOfFirst { it.toInt() == 0 }
        //todo detect charset
        val metaString = String(metadataBuffer, 0, actualSize)
        playerCallback.onMetadata(metaString)
    }

    private fun ensureFill(buffer: ByteArray, offset: Int, size: Int): Int {
        val bytes = super.read(buffer, offset, size)
        return if (bytes != -1 && size - bytes > 0) {
            bytes + ensureFill(buffer, offset + bytes, size - bytes)
        } else {
            bytes
        }
    }
}