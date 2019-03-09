package io.github.vladimirmi.internetradioplayer.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.recorder.RecorderService
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
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

    val currentRecordingObs: BehaviorRelay<Set<String>> = BehaviorRelay.createDefault(emptySet())

    val recordsObs: BehaviorRelay<List<Record>> = BehaviorRelay.createDefault(emptyList())

    val records: List<Record> get() = recordsObs.value ?: emptyList()

    fun initRecords(): Completable {
        return Completable.fromAction { recordsObs.accept(loadRecords()) }
    }

    fun startRecording(station: Station) {
        val name = getNewRecordName(station.name)

        val intent = Intent(context, RecorderService::class.java).apply {
            if (currentRecordingObs.value!!.contains(station.id)) {
                putExtra(RecorderService.EXTRA_STOP_RECORD, name)
                currentRecordingObs.accept(HashSet(currentRecordingObs.value).apply { remove(station.id) })
            } else {
                putExtra(RecorderService.EXTRA_START_RECORD, name)
                currentRecordingObs.accept(HashSet(currentRecordingObs.value).apply { add(station.id) })
            }
            data = station.uri.toUri()
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun createNewRecord(name: String): Record {
        val file = File(recordsDirectory, "$name.$RECORD_EXT")
        return Record.fromFile(file)
    }

    fun commitRecord(record: Record) {
        val newRecord = record.copy(createdAt = System.currentTimeMillis())
        val list = (recordsObs.value ?: emptyList()) + newRecord
        recordsObs.accept(list)
    }

    fun deleteRecord(record: Record): Single<Boolean> {
        return Single.fromCallable { record.file.delete() }
                .subscribeOn(Schedulers.io())
    }

    private fun loadRecords(): List<Record> {
        return recordsDirectory
                .listFiles { pathname -> pathname.extension == RECORD_EXT }
                .map { Record.fromFile(it) }
    }

    private fun getNewRecordName(stationName: String): String {
        val regex = "^$stationName(_\\d)?".toRegex()
        val list = recordsObs.value?.filter { it.name.matches(regex) } ?: emptyList()
        return if (list.isEmpty()) stationName
        else "${stationName}_${list.size}"
    }
}
