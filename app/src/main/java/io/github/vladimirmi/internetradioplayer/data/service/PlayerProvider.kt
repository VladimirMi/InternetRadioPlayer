package io.github.vladimirmi.internetradioplayer.data.service

import android.content.Context
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
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
    private val downloadManager: DownloadManager by lazy(this::createDownloadManager)
    private val downloadTracker: DownloadTracker by lazy(this::createDownloadTracker)

    fun buildDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
        return buildReadOnlyCacheDataSource(upstreamFactory, downloadCache)
    }

    fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory("userAgent")
    }

    private fun buildReadOnlyCacheDataSource(upstreamFactory: DefaultDataSourceFactory, cache: Cache)
            : CacheDataSourceFactory {
        return CacheDataSourceFactory(
                cache,
                upstreamFactory,
                FileDataSourceFactory(),
                /* eventListener= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* cacheWriteDataSinkFactory= */ null)
    }

    private fun createDownloadTracker(): DownloadTracker {
        val tracker = DownloadTracker()
        downloadManager.removeListener(tracker)
        downloadManager.addListener(tracker)
        return tracker
    }

    private fun createDownloadManager(): DownloadManager {
        val downloaderConstructorHelper = DownloaderConstructorHelper(downloadCache,
                buildHttpDataSourceFactory())
        val manager = DownloadManager(
                downloaderConstructorHelper,
                MAX_SIMULTANEOUS_DOWNLOADS,
                DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                File(downloadDirectory, DOWNLOAD_ACTION_FILE))

        manager.removeListener(downloadTracker)
        manager.addListener(downloadTracker)
        return manager
    }
}
