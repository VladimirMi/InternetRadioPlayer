package io.github.vladimirmi.radius.service

import com.google.android.exoplayer2.upstream.DataSource
import io.github.vladimirmi.radius.BuildConfig

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class HttpDataSourceFactory(private val playerCallback: PlayerCallback) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return IcyDataSource(BuildConfig.APPLICATION_ID, null, playerCallback)
    }
}