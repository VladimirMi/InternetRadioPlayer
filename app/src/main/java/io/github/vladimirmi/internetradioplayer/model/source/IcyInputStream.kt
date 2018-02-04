package io.github.vladimirmi.internetradioplayer.model.source

import io.github.vladimirmi.internetradioplayer.model.entity.Metadata
import io.github.vladimirmi.internetradioplayer.model.service.PlayerCallback
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

    override fun read(): Int {
        if (bytesBeforeMetadata == 0) {
            readMetadata()
        }
        val byte = super.read()
        bytesBeforeMetadata--
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (bytesBeforeMetadata == 0) {
            readMetadata()
        }
        val bytes = super.read(b, off, if (bytesBeforeMetadata < len) bytesBeforeMetadata else len)
        bytesBeforeMetadata -= bytes
        return bytes
    }

    private fun readMetadata() {
        bytesBeforeMetadata = window
        val size = super.read() * 16
        if (size < 1) return
        val buffer = ByteArray(size)
        super.read(buffer, 0, size)
        val actualSize = buffer.indexOfFirst { it.toInt() == 0 }
        parseMetadata(String(buffer, 0, actualSize, Charsets.UTF_8))
    }

    private fun parseMetadata(meta: String) {
        meta.split(";")
                .filter(String::isNotEmpty)
                .forEach { playerCallback.onMetadata(Metadata.create(it)) }
    }
}