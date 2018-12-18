package io.github.vladimirmi.internetradioplayer.model.manager

import android.graphics.Color
import io.github.vladimirmi.internetradioplayer.domain.model.ICONS
import io.github.vladimirmi.internetradioplayer.domain.model.Icon
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.zip.CRC32

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

private const val SIGN_IHDR_LENGTH = 33 // bytes
private const val KEY_FG_COLOR = "Foreground color"
private const val KEY_ICON_RES = "Icon resource"
private const val AUX_BYTES = 4


private class PngTextChunk(val key: String, val value: String) {

    companion object {
        const val CHUNK_TYPE = "tEXt"
        private const val SEPARATOR = '\u0000'

        fun create(chunkType: ByteArray, data: ByteArray, crc: Int): PngTextChunk? {
            if (chunkType.toString(Charset.defaultCharset()) != CHUNK_TYPE
                    || CRC32().apply { update(data) }.value.toInt() != crc) {
                return null
            }
            val (key, value) = data.toString(Charset.defaultCharset()).split(SEPARATOR)
            return PngTextChunk(key, value)
        }
    }

    fun getByteArray(): ByteArray {
        val dataBytes = "$key$SEPARATOR$value".toByteArray()
        val lengthBytes = ByteBuffer.allocate(AUX_BYTES).putInt(dataBytes.size).array()
        val chunkTypeBytes = CHUNK_TYPE.toByteArray()
        val crc = CRC32().apply { update(dataBytes) }
        val crcBytes = ByteBuffer.allocate(AUX_BYTES).putInt(crc.value.toInt()).array()

        return ByteArrayOutputStream().apply {
            write(lengthBytes)
            write(chunkTypeBytes)
            write(dataBytes)
            write(crcBytes)
        }.toByteArray()
    }
}


//fun File.encode(icon: Icon) {
//    ByteArrayOutputStream().use { outS ->
//        icon.bitmap.compress(Bitmap.CompressFormat.PNG, 100, outS)
//        val pngBytes = outS.toByteArray()
//
//        FileOutputStream(this).use {
//            it.write(pngBytes, 0, SIGN_IHDR_LENGTH)
//            it.write(PngTextChunk(KEY_OPTION, icon.option.name).getByteArray())
//            when (icon) {
//                is IconRes -> {
//                    it.write(PngTextChunk(KEY_ICON_RES, icon.res.name).getByteArray())
//                    it.write(PngTextChunk(KEY_FG_COLOR, icon.foregroundColor.toString()).getByteArray())
//                }
//            }
////            it.write(PngTextChunk(KEY_BG_COLOR, icon.backgroundColor.toString()).getByteArray())
////            if (icon.option == IconOption.TEXT) {
////                it.write(PngTextChunk(KEY_TEXT, icon.text).getByteArray())
////            }
//            it.write(pngBytes, SIGN_IHDR_LENGTH, pngBytes.size - SIGN_IHDR_LENGTH)
//        }
//    }
//}

fun File.decode(): Icon {
    val pngBytes = readBytes()
    val wrapped = ByteBuffer.wrap(pngBytes)
            .position(SIGN_IHDR_LENGTH) as ByteBuffer

    var iconRes = ICONS[0]
    var foregroundColor = Color.BLACK

    var chunk = nextChunkFromBuffer(wrapped)
    while (chunk != null) {
        when (chunk.key) {
            KEY_ICON_RES -> iconRes = chunk.value.substringAfterLast("_", "1").toInt() - 1
            KEY_FG_COLOR -> foregroundColor = chunk.value.toInt()
        }
        chunk = nextChunkFromBuffer(wrapped)
    }
    return Icon(iconRes, Color.WHITE, foregroundColor)
}

private fun nextChunkFromBuffer(buffer: ByteBuffer): PngTextChunk? {
    val length = buffer.int
    val chunkType = ByteArray(AUX_BYTES).also { buffer.get(it) }
    val body = ByteArray(length).also { buffer.get(it) }
    val crc = buffer.int
    return PngTextChunk.create(chunkType, body, crc)
}
