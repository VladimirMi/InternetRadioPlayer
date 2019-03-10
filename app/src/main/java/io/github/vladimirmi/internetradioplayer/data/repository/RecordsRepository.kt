package io.github.vladimirmi.internetradioplayer.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.recorder.RecorderService
import io.github.vladimirmi.internetradioplayer.data.service.recorder.RecordersPool
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import io.github.vladimirmi.internetradioplayer.utils.MessageException
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

    val currentRecordingUrisObs: BehaviorRelay<Set<String>> = BehaviorRelay.createDefault(emptySet())
    val recordsObs: BehaviorRelay<List<Record>> = BehaviorRelay.create()

    var records: List<Record>
        get() = recordsObs.value ?: emptyList()
        set(value) {
            recordsObs.accept(value.sortedBy(Record::createdAt))
        }

    fun initRecords(): Completable {
        return Completable.fromAction { records = loadRecords() }
                .subscribeOn(Schedulers.io())
    }

    fun startStopRecording(station: Station) {
        when {
            currentRecordingUrisObs.value!!.contains(station.uri) -> stopRecording(station.uri.toUri())
            currentRecordingUrisObs.value!!.size < RecordersPool.MAX_RECORDERS -> startRecording(station)
            else -> throw MessageException("The maximum number of simultaneous recordings is ${RecordersPool.MAX_RECORDERS}")
        }
    }

    private fun startRecording(station: Station) {
        val intent = Intent(context, RecorderService::class.java).apply {
            putExtra(RecorderService.EXTRA_START_RECORD, station.name)
            data = station.uri.toUri()
        }
        context.startService(intent)
    }

    fun stopRecording(uri: Uri) {
        val intent = Intent(context, RecorderService::class.java).apply {
            putExtra(RecorderService.EXTRA_STOP_RECORD, "")
            data = uri
        }
        context.startService(intent)
    }

    fun createNewRecord(name: String): Record {
        val newName = getNewRecordName(name)
        val file = File(recordsDirectory, "$newName.$RECORD_EXT")
        return Record.newRecord(file)
    }

    fun deleteRecord(record: Record): Single<Boolean> {
        return Single.fromCallable { record.file.delete() }
                .subscribeOn(Schedulers.io())
    }

    fun addToCurrentRecording(stationUri: Uri) {
        currentRecordingUrisObs.accept(currentRecordingUrisObs.value!! + stationUri.toString())
    }

    fun removeFromCurrentRecording(stationUri: Uri) {
        currentRecordingUrisObs.accept(currentRecordingUrisObs.value!! - stationUri.toString())
    }

    private fun loadRecords(): List<Record> {
        return recordsDirectory
                .listFiles { pathname -> pathname.extension == RECORD_EXT }
                .map { Record.fromFile(it) }
    }

    private fun getNewRecordName(stationName: String): String {
        val regex = "^$stationName(_\\d)?".toRegex()
        val list = records.filter { it.name.matches(regex) }
        return if (list.isEmpty()) stationName
        else "${stationName}_${list.size}"
    }
}
