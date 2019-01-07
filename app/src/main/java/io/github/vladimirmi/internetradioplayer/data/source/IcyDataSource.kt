package io.github.vladimirmi.internetradioplayer.data.source

import android.net.Uri
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import io.github.vladimirmi.internetradioplayer.data.service.PlayerCallback
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */
private const val CONNECT_TIMEOUT_MILLIS = 5000
private const val READ_TIMEOUT_MILLIS = 5000


class IcyDataSource(private val userAgent: String,
                    private val playerCallback: PlayerCallback)
    : BaseDataSource(true) {

    private var inputStream: InputStream? = null
    private var connection: HttpURLConnection? = null


    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)
        try {
            connection = makeConnection(dataSpec)
        } catch (e: IOException) {
            throw IOException("Unable to connect to " + dataSpec.uri.toString(), e)
        }

        val responseCode: Int
        try {
            responseCode = connection!!.responseCode
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw IOException("Unable to connect to " + dataSpec.uri.toString(), e)
        }

        // Check for a valid response code.
        if (responseCode !in IntRange(200, 299)) {
            val headers = connection!!.headerFields
            closeConnectionQuietly()
            throw IOException(String.format("Invalid response code %d: %s", responseCode, headers))
        }

        try {
            inputStream = getInputStream(connection!!)
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw e
        }

        transferStarted(dataSpec)
        return dataSpec.length
    }

    override fun getUri(): Uri? {
        return connection?.let { Uri.parse(it.url.toString()) }
    }

    override fun close() {
//        timerTask?.cancel()
//        timerTask = null

        try {
            inputStream?.close()
        } finally {
            transferEnded()
            inputStream = null
            closeConnectionQuietly()
        }
    }

//    private var readed = 0
//    private var timerTask: TimerTask? = null
//    private var count = 0
//    private var kbps = 0

    override fun read(buffer: ByteArray?, offset: Int, readLength: Int): Int {
//        val read = inputStream?.read(buffer, offset, readLength) ?: 0
//        readed += read
//
//        if (timerTask == null) {
//            timerTask = Timer().scheduleAtFixedRate(5000, 1000) {
//                if (count == 0) {
//                    count = 1
//                    readed = 0
//                }
//                val calcKbps = readed * 8 / 1000
//                if (kbps == 0) {
//                    kbps = calcKbps
//                } else {
//                    count++
//                    kbps += calcKbps
//                }
//                Timber.e("read: ${kbps / count}kbps")
//                readed = 0
//            }
//        }
//
//        return read
        val read = inputStream?.read(buffer, offset, readLength) ?: 0
        bytesTransferred(read)
        return read
    }

    @Throws(IOException::class)
    private fun getInputStream(connection: HttpURLConnection): InputStream {
        val inputStream = connection.inputStream
        val metaWindow = connection.getHeaderFieldInt("icy-metaint", 0)

        return if (metaWindow > 0) {
            IcyInputStream(inputStream, metaWindow, playerCallback)
        } else {
            Timber.d("stream does not support icy metadata")
            playerCallback.onMetadata("")
            inputStream
        }
    }

    @Throws(IOException::class)
    private fun makeConnection(dataSpec: DataSpec): HttpURLConnection {
        val connection = URL(dataSpec.uri.toString()).openConnection() as HttpURLConnection

        return connection.apply {
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", userAgent)
            setRequestProperty("Icy-Metadata", "1")

            connect()
        }
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private fun closeConnectionQuietly() {
        try {
            connection?.disconnect()
        } catch (e: IOException) {
            Timber.w(e, "Unexpected error while disconnecting")
        }
        connection = null
    }
}
