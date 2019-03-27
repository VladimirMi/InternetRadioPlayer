package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface PlayerView : BaseView {

    fun setMedia(media: Media)

    fun setFavorite(isFavorite: Boolean)

    fun setMetadata(metadata: String)

    fun setStatus(resId: Int)

    fun showPlaying(isPlaying: Boolean)

    fun showPrevious()

    fun showNext()

    fun setDuration(duration: Long)

    fun setPosition(position: Long)

    fun incrementPositionBy(duration: Long)

    fun enableSeek(isEnabled: Boolean)

    fun enableSkip(isEnabled: Boolean)
}
