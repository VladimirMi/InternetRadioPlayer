package io.github.vladimirmi.internetradioplayer.domain.model

import android.media.MediaMetadataRetriever
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.Formats
import java.io.File


/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

data class Record(override val id: String,
                  override val name: String,
                  override val uri: String,
                  val file: File,
                  val createdAt: Long,
                  val duration: Long) : Media {

    companion object {
        fun fromFile(file: File): Record {
            val uri = file.toURI().toString()
            return Record(
                    id = uri,
                    name = file.name.substringBeforeLast('.'),
                    uri = uri,
                    file = file,
                    createdAt = file.lastModified(),
                    duration = getDuration(file)
            )
        }

        fun newRecord(file: File): Record {
            val uri = file.toURI().toString()
            return Record(
                    id = uri,
                    name = file.name.substringBeforeLast('.'),
                    uri = uri,
                    file = file,
                    createdAt = 0,
                    duration = 0
            )
        }

        private fun getDuration(file: File): Long {
            val mmr = Scopes.app.getInstance(MediaMetadataRetriever::class.java)
            mmr.setDataSource(file.absolutePath)
            return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        }
    }

    override val remoteId = id
    val createdAtString = Formats.dateTime(createdAt)
    val durationString = Formats.duration(duration)

    fun calculateDuration() = getDuration(file)
}


