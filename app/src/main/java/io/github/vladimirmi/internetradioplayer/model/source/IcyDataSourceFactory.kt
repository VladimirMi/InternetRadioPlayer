package io.github.vladimirmi.internetradioplayer.model.source

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.github.vladimirmi.internetradioplayer.model.service.PlayerCallback

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyDataSourceFactory(private val playerCallback: PlayerCallback, private val userAgent: String)
    : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return IcyDataSource(userAgent, null, playerCallback)
    }
}
