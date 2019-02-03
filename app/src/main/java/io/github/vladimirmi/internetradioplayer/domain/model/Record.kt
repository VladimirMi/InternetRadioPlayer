package io.github.vladimirmi.internetradioplayer.domain.model

import java.io.File

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

data class Record(override val id: String,
                  override val name: String,
                  override val uri: String,
                  val file: File) : Media