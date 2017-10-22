package io.github.vladimirmi.radius.data.source

import com.google.android.exoplayer2.upstream.DataSource
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.data.service.PlayerCallback
import io.github.vladimirmi.radius.data.source.IcyDataSource

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyDataSourceFactory(private val playerCallback: PlayerCallback) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return IcyDataSource(BuildConfig.APPLICATION_ID, null, playerCallback)
    }
}