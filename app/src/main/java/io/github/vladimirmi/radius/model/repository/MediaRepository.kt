package io.github.vladimirmi.radius.model.repository

import io.github.vladimirmi.radius.model.data.Preferences
import io.github.vladimirmi.radius.model.entity.Media
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaRepository
@Inject constructor(private val prefs: Preferences) {

    fun getMediaList(): List<Media> {
        return emptyList()
    }
}