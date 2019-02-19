package io.github.vladimirmi.internetradioplayer.domain.model

import java.io.File
import java.util.*

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

data class Record(override val id: String,
                  override val name: String,
                  override val uri: String,
                  val file: File,
                  val createdAt: Long) : Media {

    companion object {
        fun fromFile(file: File): Record {
            return Record(
                    UUID.randomUUID().toString(),
                    file.name.substringBeforeLast('.'),
                    file.toURI().toString(),
                    file,
                    file.lastModified()
            )
        }
    }
}