package io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo

import io.github.vladimirmi.internetradioplayer.domain.model.MediaInfo
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */
interface MediaInfoView : BaseView {

    fun setRecording(isRecording: Boolean)

    fun setMediaInfo(mediaInfo: MediaInfo)
}