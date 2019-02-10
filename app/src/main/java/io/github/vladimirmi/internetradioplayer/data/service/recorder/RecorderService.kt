package io.github.vladimirmi.internetradioplayer.data.service.recorder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.github.vladimirmi.internetradioplayer.di.Scopes
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.02.2019.
 */

class RecorderService : Service() {

    companion object {
        const val EXTRA_START_RECORD = "EXTRA_START_RECORD"
        const val EXTRA_STOP_RECORD = "EXTRA_STOP_RECORD"
    }

    @Inject lateinit var recorder: Recorder
    private lateinit var notification: RecorderNotification

    override fun onCreate() {
        Toothpick.inject(this, Scopes.app)
        notification = RecorderNotification(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent == null -> stopSelf()
            intent.hasExtra(EXTRA_START_RECORD) -> {
                recorder.startRecord(intent.data!!)
                notification.start()
            }
            intent.hasExtra(EXTRA_STOP_RECORD) -> {
                recorder.stopRecord()
                notification.stop()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        recorder.stopRecord()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}