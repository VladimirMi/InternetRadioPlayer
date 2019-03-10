package io.github.vladimirmi.internetradioplayer.data.service.recorder

import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.util.ReusableBufferedOutputStream
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.domain.interactor.RecordsInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 03.02.2019.
 */

private const val DEFAULT_BUFFER_SIZE = 20480
private const val DEFAULT_MAX_FILE_SIZE: Long = 50 * 1024 * 1024

class RecorderDataSink
@Inject constructor(private val interactor: RecordsInteractor) : DataSink {

    private var dataSpecBytesWritten: Long = 0
    private var outputStreamBytesWritten: Long = 0
    private var record: Record? = null

    private var outputStream: ReusableBufferedOutputStream? = null
    private lateinit var fileOutputStream: FileOutputStream

    private lateinit var dataSpec: DataSpec

    override fun open(dataSpec: DataSpec) {
        dataSpecBytesWritten = 0
        this.dataSpec = dataSpec
        openNextOutputStream()
    }

    override fun write(buffer: ByteArray?, offset: Int, length: Int) {
        if (outputStream == null) return

        var bytesWritten = 0
        while (bytesWritten < length) {
            if (outputStreamBytesWritten == DEFAULT_MAX_FILE_SIZE) {
                close()
                //todo protect by MAX_FILE_SIZE once (implement scheduling for recording)
//                openNextOutputStream()
            }
            val bytesToWrite = Math.min(length - bytesWritten,
                    (DEFAULT_MAX_FILE_SIZE - outputStreamBytesWritten).toInt())

            outputStream!!.write(buffer, offset + bytesWritten, bytesToWrite)
            bytesWritten += bytesToWrite
            outputStreamBytesWritten += bytesToWrite.toLong()
            dataSpecBytesWritten += bytesToWrite.toLong()
        }
    }

    override fun close() {
        try {
            closeInternal()
        } finally {
            interactor.stopRecording(dataSpec.uri)
        }
    }

    private fun closeInternal() {
        if (outputStream == null) return

        try {
            outputStream!!.flush()
            fileOutputStream.fd.sync()
            interactor.commitRecord(dataSpec.uri, record!!)
        } catch (ex: IOException) {
            interactor.deleteRecord(record!!).subscribeX()
        } finally {
            Util.closeQuietly(outputStream)
            outputStream = null
        }
    }

    private fun openNextOutputStream() {
//        val size = if (dataSpec.length.toInt() == C.LENGTH_UNSET) {
//            DEFAULT_MAX_FILE_SIZE
//        } else {
//            Math.min(dataSpec.length - dataSpecBytesWritten, DEFAULT_MAX_FILE_SIZE)
//        }
        //todo ensure that there is enough space
        record = interactor.createNewRecord(dataSpec.uri, dataSpec.key ?: "new_record")
        fileOutputStream = FileOutputStream(record!!.file)

        if (outputStream == null) {
            outputStream = ReusableBufferedOutputStream(fileOutputStream, DEFAULT_BUFFER_SIZE)
        } else {
            outputStream!!.reset(fileOutputStream)
        }
        outputStreamBytesWritten = 0
    }
}