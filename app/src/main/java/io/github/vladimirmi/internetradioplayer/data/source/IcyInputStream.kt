package io.github.vladimirmi.internetradioplayer.data.source

import io.github.vladimirmi.internetradioplayer.data.service.Metadata
import io.github.vladimirmi.internetradioplayer.data.service.PlayerCallback
import java.io.FilterInputStream
import java.io.InputStream

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyInputStream(inS: InputStream,
                     private val window: Int,
                     private val playerCallback: PlayerCallback)
    : FilterInputStream(inS) {

    private var bytesBeforeMetadata = window
    private var metadata: Metadata? = null
    private var buffer = ByteArray(128)

    override fun read(): Int {
        val byte = super.read()
        if (--bytesBeforeMetadata == 0) readMetadata()
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val bytes = super.read(b, off, if (bytesBeforeMetadata < len) bytesBeforeMetadata else len)
        bytesBeforeMetadata -= bytes
        if (bytesBeforeMetadata == 0) readMetadata()
        return bytes
    }

    private fun readMetadata() {
        bytesBeforeMetadata = window
        val size = super.read() * 16
        if (size < 1) return
        if (size > buffer.size) {
            buffer = ByteArray(size)
        }
        ensureRead(buffer, 0, size)
        val actualSize = buffer.indexOfFirst { it.toInt() == 0 }
        parseMetadata(String(buffer, 0, actualSize))
    }

    private fun parseMetadata(meta: String) {
        val metadata = Metadata.create(meta)
        if (this.metadata != metadata) {
            playerCallback.onMetadata(metadata)
            this.metadata = metadata
        }
    }

    private fun ensureRead(buffer: ByteArray, offset: Int, size: Int): Int {
        val n = super.read(buffer, offset, size)

        return if (n != -1 && size - n > 0) {
            n + ensureRead(buffer, offset + n, size - n)
        } else {
            n
        }
    }
}
