package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface PlayerView : BaseView {

    fun showPlayerView(visible: Boolean)

    fun setStation(station: Station)

    fun setRecord(record: Record)

    fun setFavorite(isFavorite: Boolean)

    fun setMetadata(artist: String, title: String)

    fun setSimpleMetadata(metadata: String)

    fun setStatus(resId: Int)

    fun showPlaying(isPlaying: Boolean)

    fun showPrevious()

    fun showNext()

    fun setRecording(isRecording: Boolean)

    fun setGroup(group: String?)

    fun setDuration(duration: Long)

    fun setPosition(position: Long)

    fun increasePosition(duration: Long)
}
