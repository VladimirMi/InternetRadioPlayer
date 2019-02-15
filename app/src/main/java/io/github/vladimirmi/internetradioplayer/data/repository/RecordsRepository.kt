package io.github.vladimirmi.internetradioplayer.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.recorder.RecorderService
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

private const val RECORDS_DIRECTORY = "records"
private const val RECORD_EXT = "mp3"

class RecordsRepository
@Inject constructor(private val context: Context) {

    private val recordsDirectory: File by lazy {
        val dir = File(context.getExternalFilesDir(null), RECORDS_DIRECTORY)
        dir.mkdir()
        dir
    }

    private var recording = false

    val recordsObs by lazy {
        BehaviorRelay.createDefault(initRecords())
    }

    fun startRecord(station: Station) {
        val intent = Intent(context, RecorderService::class.java).apply {
            if (recording) {
                putExtra(RecorderService.EXTRA_STOP_RECORD, 0)
            } else {
                putExtra(RecorderService.EXTRA_START_RECORD, 0)
            }
            data = station.uri.toUri()
        }
        ContextCompat.startForegroundService(context, intent)
        recording = !recording
    }

    fun createNewRecord(name: String): Record {
        Timber.e("createNewRecord: $name")
        val file = File(recordsDirectory, "$name.$RECORD_EXT")
        return Record(
                UUID.randomUUID().toString(),
                name,
                file.toURI().toString(),
                file
        )
    }

    fun commitRecord(record: Record) {
        Timber.e("commitRecord: $record")
        val list = (recordsObs.value ?: emptyList()) + record
        recordsObs.accept(list)
    }

    fun deleteRecord(record: Record) {
        Timber.e("deleteRecord: $record")
    }

    private fun initRecords(): List<Record> {
        return recordsDirectory
                .listFiles { pathname -> pathname.extension == RECORD_EXT }
                .map { Record.fromFile(it) }
    }
}
