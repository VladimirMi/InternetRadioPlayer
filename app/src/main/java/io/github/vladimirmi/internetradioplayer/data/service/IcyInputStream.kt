package io.github.vladimirmi.internetradioplayer.data.service

import java.io.FilterInputStream
import java.io.InputStream

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyInputStream(inS: InputStream,
                     private val window: Int,
                     private val playerCallback: PlayerCallback? = null)
    : FilterInputStream(inS) {

    private var buffer = ByteArray(128)
    private var bytesBeforeMetadata = window

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
        ensureFill(buffer, 0, size)
        val actualSize = buffer.indexOfFirst { it.toInt() == 0 }
        //todo detect charset
        val metaString = String(buffer, 0, actualSize)
        playerCallback?.onMetadata(metaString)
    }

    private fun ensureFill(buffer: ByteArray, offset: Int, size: Int): Int {
        val n = super.read(buffer, offset, size)

        return if (n != -1 && size - n > 0) {
            n + ensureFill(buffer, offset + n, size - n)
        } else {
            n
        }
    }
}
