package io.github.vladimirmi.radius.model.manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.presentation.iconpicker.IconOption
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.zip.CRC32

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

private const val SIGN_IHDR_LENGTH = 33 // bytes
private const val KEY_BG_COLOR = "Background color"
private const val KEY_TEXT_COLOR = "Text color"
private const val KEY_TEXT = "Text"
private const val KEY_OPTION = "Option"


class PngTextChunk(val key: String, val value: String) {

    companion object {
        const val CHUNK_TYPE = "tEXt"
        private const val SEPARATOR = '\u0000'

        fun create(chunkType: ByteArray, data: ByteArray, crc: Int): PngTextChunk? {
            val calcCrc = CRC32().apply { update(chunkType + data) }
            if (calcCrc.value.toInt() != crc) return null
            val (key, value) = data.toString(Charset.defaultCharset()).split(SEPARATOR)
            return PngTextChunk(key, value)
        }
    }

    fun getByteArray(): ByteArray {
        val dataBytes = "$key$SEPARATOR$value".toByteArray()
        val lengthBytes = ByteBuffer.allocate(4).putInt(dataBytes.size).array()
        val chunkTypeBytes = CHUNK_TYPE.toByteArray()
        val crc = CRC32().apply { update(dataBytes) }
        val crcBytes = ByteBuffer.allocate(4).putInt(crc.value.toInt()).array()

        return ByteArrayOutputStream().apply {
            write(lengthBytes)
            write(chunkTypeBytes)
            write(dataBytes)
            write(crcBytes)
        }.toByteArray()
    }
}


fun File.encode(icon: Icon) {
    ByteArrayOutputStream().use { outS ->
        icon.bitmap.compress(Bitmap.CompressFormat.PNG, 100, outS)
        val pngBytes = outS.toByteArray()

        FileOutputStream(this).use {
            it.write(pngBytes, 0, SIGN_IHDR_LENGTH)
            it.write(PngTextChunk(KEY_BG_COLOR, icon.backGroundColor.toString()).getByteArray())
            it.write(PngTextChunk(KEY_TEXT_COLOR, icon.textColor.toString()).getByteArray())
            it.write(PngTextChunk(KEY_TEXT, icon.text).getByteArray())
            it.write(PngTextChunk(KEY_OPTION, icon.option.name).getByteArray())
            it.write(pngBytes, SIGN_IHDR_LENGTH, pngBytes.size - SIGN_IHDR_LENGTH)
        }
    }
}

fun File.decode(): Icon {
    val pngBytes = readBytes()
    val wrapped = ByteBuffer.wrap(pngBytes)
            .position(SIGN_IHDR_LENGTH) as ByteBuffer

    var backGroundColor = Color.LTGRAY
    var textColor = Color.BLACK
    var text = ""
    var option = IconOption.DEFAULT

    var chunk = nextChunkFromBuffer(wrapped)
    while (chunk != null) {
        when (chunk.key) {
            KEY_BG_COLOR -> backGroundColor = chunk.value.toInt()
            KEY_TEXT_COLOR -> textColor = chunk.value.toInt()
            KEY_TEXT -> text = chunk.value
            KEY_OPTION -> option = IconOption.valueOf(chunk.value)
        }
        chunk = nextChunkFromBuffer(wrapped)
    }

    val bitmap = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)
    return Icon(path, bitmap, backGroundColor, textColor, text, option)
}

private fun nextChunkFromBuffer(buffer: ByteBuffer): PngTextChunk? {
    val length = buffer.int
    val chunkType = ByteArray(4).also { buffer.get(it) }
    if (chunkType.toString(Charset.defaultCharset()) != PngTextChunk.CHUNK_TYPE) return null
    val body = ByteArray(length).also { buffer.get(it) }
    val crc = buffer.int
    return PngTextChunk.create(chunkType, body, crc)
}