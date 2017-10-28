package io.github.vladimirmi.radius.model.source

import com.google.android.exoplayer2.upstream.DataSource
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.model.service.PlayerCallback

/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

class IcyDataSourceFactory(private val playerCallback: PlayerCallback) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return IcyDataSource(BuildConfig.APPLICATION_ID, null, playerCallback)
    }
}