package io.github.vladimirmi.internetradioplayer.data.source

import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSource
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
private const val connectTimeoutMillis = 5000
private const val readTimeoutMillis = 5000


class IcyDataSource(private val userAgent: String,
                    private val playerCallback: PlayerCallback)
    : DataSource {

    private var inputStream: InputStream? = null
    private var connection: HttpURLConnection? = null

    override fun open(dataSpec: DataSpec): Long {
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
        if (responseCode < 200 || responseCode > 299) {
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

        return dataSpec.length
    }

    override fun getUri(): Uri? {
        return connection?.let { Uri.parse(it.url.toString()) }
    }

    override fun close() {
        try {
            inputStream?.close()
        } finally {
            inputStream = null
            closeConnectionQuietly()
        }
    }

    override fun read(buffer: ByteArray?, offset: Int, readLength: Int): Int {
        return inputStream?.read(buffer, offset, readLength) ?: 0
    }

    @Throws(IOException::class)
    private fun getInputStream(connection: HttpURLConnection): InputStream {
        val inputStream = connection.inputStream
        val metaWindow = connection.getHeaderFieldInt("icy-metaint", 0)

        return if (metaWindow > 0) {
            IcyInputStream(inputStream, metaWindow, playerCallback)
        } else {
            Timber.d("stream does not support icy metadata")
            //todo Implement
//            playerCallback.onMetadata(Metadata.UNSUPPORTED)
            inputStream
        }
    }

    @Throws(IOException::class)
    private fun makeConnection(dataSpec: DataSpec): HttpURLConnection {
        val connection = URL(dataSpec.uri.toString()).openConnection() as HttpURLConnection

        return connection.apply {
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
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
        } catch (e: Exception) {
            Timber.w(e, "Unexpected error while disconnecting")
        }
        connection = null
    }
}
