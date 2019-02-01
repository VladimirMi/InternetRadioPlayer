package io.github.vladimirmi.internetradioplayer.data.service

import android.content.Context
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.R
import timber.log.Timber
import java.io.File

/**
 * Created by Vladimir Mikhalev 31.01.2019.
 */

private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
private const val DOWNLOAD_ACTION_FILE = "actions"
private const val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"
private const val MAX_SIMULTANEOUS_DOWNLOADS = 2

class PlayerProvider(private val context: Context) {

    private val downloadDirectory: File by lazy {
        context.getExternalFilesDir(null) ?: context.filesDir
    }
    private val downloadCache: Cache by lazy {
        val downloadContentDirectory = File(downloadDirectory, DOWNLOAD_CONTENT_DIRECTORY)
        SimpleCache(downloadContentDirectory, NoOpCacheEvictor())
    }
    val downloadManager: DownloadManager by lazy(this::createDownloadManager)
    val downloadTracker: DownloadTracker by lazy(this::createDownloadTracker)
    private val userAgent = Util.getUserAgent(context, context.getString(R.string.full_app_name))


    fun buildHttpDataSourceFactory(): DataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent)
    }

    fun buildCacheDataSource(): ForcedCacheDataSource {
        val cacheDataSource = CacheDataSource(downloadCache,
                buildHttpDataSourceFactory().createDataSource(),
                FileDataSourceFactory().createDataSource(),
                CacheDataSink(downloadCache, CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null)
        return ForcedCacheDataSource(cacheDataSource)
    }

    private fun createDownloadTracker(): DownloadTracker {
        Timber.e("keys: ${downloadCache.keys}")
        downloadCache.keys.forEach {
            Timber.e("key: $it; ${downloadCache.getCachedLength(it, 0, Long.MAX_VALUE)}")
        }

        return DownloadTracker()
    }

    private fun createDownloadManager(): DownloadManager {
        val downloaderConstructorHelper = DownloaderConstructorHelper(downloadCache,
                buildHttpDataSourceFactory())

        return DownloadManager(
                downloaderConstructorHelper,
                MAX_SIMULTANEOUS_DOWNLOADS,
                DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                File(downloadDirectory, DOWNLOAD_ACTION_FILE))
    }
}
