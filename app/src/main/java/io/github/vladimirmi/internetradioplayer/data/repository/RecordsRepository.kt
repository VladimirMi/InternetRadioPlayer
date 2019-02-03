package io.github.vladimirmi.internetradioplayer.data.repository

import android.content.Context
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import java.io.File
import java.util.*

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

private const val RECORDS_DIRECTORY = "downloads"

class RecordsRepository(private val context: Context) {

    private val recordsDirectory: File by lazy {
        val dir = File(context.getExternalFilesDir(null), RECORDS_DIRECTORY)
        dir.mkdir()
        dir
    }

    val recordsObs = BehaviorRelay.createDefault(emptyList<Record>())

    fun createNewRecord(name: String): Record {
        val file = File(recordsDirectory, name)
        return Record(
                UUID.randomUUID().toString(),
                name,
                file.toURI().toString(),
                file
        )
    }

    fun commitRecord(record: Record) {
        val list = (recordsObs.value ?: emptyList()) + record
        recordsObs.accept(list)
    }

    fun deleteRecord() {}
}