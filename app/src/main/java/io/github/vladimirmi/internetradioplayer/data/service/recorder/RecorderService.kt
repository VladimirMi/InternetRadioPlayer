package io.github.vladimirmi.internetradioplayer.data.service.recorder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 09.02.2019.
 */

class RecorderService : Service() {

    companion object {
        const val EXTRA_START_RECORD = "EXTRA_START_RECORD"
        const val EXTRA_STOP_RECORD = "EXTRA_STOP_RECORD"
    }

    private lateinit var recorders: RecordersPool

    override fun onCreate() {
        recorders = RecordersPool(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent == null -> stopSelf()
            intent.hasExtra(EXTRA_START_RECORD) -> {
                val name = intent.getStringExtra(EXTRA_START_RECORD)
                recorders.startRecord(intent.data!!, name)
            }
            intent.hasExtra(EXTRA_STOP_RECORD) -> {
                recorders.stopRecord(intent.data!!)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Timber.d("onDestroy: ")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}