package io.github.vladimirmi.internetradioplayer.data.service.recorder

import android.app.Service
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.di.Scopes

/**
 * Created by Vladimir Mikhalev 10.03.2019.
 */

class RecordersPool(private val service: Service) {

    companion object {
        const val MAX_RECORDERS = 3
        const val NOTIFICATION_ID_OFFSET = 1000
    }

    private val inUse = HashMap<Uri, Recorder>(MAX_RECORDERS)
    private var available = MAX_RECORDERS
    private val notificationFactory = RecorderNotificationFactory(service)
    private val notifications = HashMap<Uri, RecorderNotification>(MAX_RECORDERS)

    fun startRecord(name: String, uri: Uri) {
        if (available == 0) return
        val recorder = createRecorder()
        recorder.startRecord(name, uri)
        inUse[uri] = recorder
        val notificationId = NOTIFICATION_ID_OFFSET + available
        val notification = notificationFactory.createAndStart(notificationId, name, notifications.isEmpty())
        notifications[uri] = notification
        available -= 1
    }

    fun stopRecord(uri: Uri) {
        inUse[uri]?.stopRecord()
        inUse.remove(uri)
        notifications[uri]?.let { notification ->
            // if that notification is started with service.startForeground()
            // start another in foreground that current notification can be cancelled
            if (notification.isForeground) {
                notifications.filter { !it.value.isForeground }.entries.firstOrNull()?.let {
                    notificationFactory.startForeground(it.value)
                    notifications[it.key] = it.value.copy(isForeground = true)
                }
            }
            notificationFactory.cancel(notification)
        }
        notifications.remove(uri)
        available += 1
        if (available == MAX_RECORDERS) service.stopForeground(true)
    }

    private fun createRecorder(): Recorder {
        return Scopes.app.getInstance(Recorder::class.java)
    }
}