package io.github.vladimirmi.internetradioplayer.data.utils

import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.DataSpec

/**
 * Created by Vladimir Mikhalev 03.02.2019.
 */

private const val DEFAULT_BUFFER_SIZE = 20480

class Recorder : DataSink {

    override fun open(dataSpec: DataSpec?) {
        TODO("not implemented")
    }

    override fun write(buffer: ByteArray?, offset: Int, length: Int) {
        TODO("not implemented")
    }

    override fun close() {
        TODO("not implemented")
    }
}