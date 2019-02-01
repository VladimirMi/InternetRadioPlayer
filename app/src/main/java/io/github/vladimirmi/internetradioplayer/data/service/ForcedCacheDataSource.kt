package io.github.vladimirmi.internetradioplayer.data.service

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 01.02.2019.
 */

class ForcedCacheDataSource(private val cacheDataSource: CacheDataSource)
    : DataSource by cacheDataSource {


    override fun open(dataSpec: DataSpec): Long {
        val spec = DataSpec(dataSpec.uri,
                dataSpec.httpMethod,
                dataSpec.httpBody,
                dataSpec.absoluteStreamPosition,
                dataSpec.position,
                dataSpec.length,
                dataSpec.key,
                dataSpec.flags or DataSpec.FLAG_ALLOW_CACHING_UNKNOWN_LENGTH
        )

        Timber.d("open: $spec")
        val open = cacheDataSource.open(spec)
        Timber.e("open: $open")
        return open
    }
}