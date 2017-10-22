package io.github.vladimirmi.radius.service

import java.io.FilterInputStream
import java.io.InputStream

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyInputStream(inS: InputStream,
                     private val window: Int,
                     private val playerCallback: PlayerCallback)
    : FilterInputStream(inS) {

    private var remainingBytes = window

    override fun read(): Int {
        val byte = super.read()
        if (--remainingBytes == 0) readMetadata()
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val bytes = super.read(b, off, if (remainingBytes < len) remainingBytes else len)
        if (remainingBytes == bytes) {
            readMetadata()
        } else {
            remainingBytes -= bytes
        }
        return bytes
    }

    private fun readMetadata() {
        remainingBytes = window
        val size = `in`.read() * 16
        if (size < 1) return
        val buffer = ByteArray(size)
        `in`.read(buffer)
        val actualSize = buffer.indexOfFirst { it.toInt() == 0 }
        parseMetadata(String(buffer, 0, actualSize, Charsets.UTF_8))
    }

    private fun parseMetadata(meta: String) {
        meta.split(";")
                .map { keyValue -> keyValue.split("=").map { it.trim(' ', '\'') } }
                .filter { kv -> kv.size == 2 && kv.all { it.isNotEmpty() } }
                .forEach { playerCallback.onMetadata(it[0], it[1]) }
    }
}