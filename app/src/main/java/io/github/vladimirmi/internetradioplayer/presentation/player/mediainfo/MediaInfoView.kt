package io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */
interface MediaInfoView : BaseView {

    fun setRecording(isRecording: Boolean)

    fun setGroup(group: String)

    fun setStation(station: Station)
    fun setRecord(record: Record)
}