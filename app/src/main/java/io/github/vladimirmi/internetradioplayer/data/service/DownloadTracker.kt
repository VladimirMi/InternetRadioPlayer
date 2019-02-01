package io.github.vladimirmi.internetradioplayer.data.service

import com.google.android.exoplayer2.offline.DownloadManager
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 01.02.2019.
 */

class DownloadTracker : DownloadManager.Listener {

    override fun onInitialized(downloadManager: DownloadManager?) {
        // Do nothing.
    }

    override fun onTaskStateChanged(downloadManager: DownloadManager,
                                    taskState: DownloadManager.TaskState) {
        Timber.e("onTaskStateChanged: ${DownloadManager.TaskState.getStateString(taskState.state)}")
    }

    override fun onIdle(downloadManager: DownloadManager?) {
        // Do nothing.
    }
}