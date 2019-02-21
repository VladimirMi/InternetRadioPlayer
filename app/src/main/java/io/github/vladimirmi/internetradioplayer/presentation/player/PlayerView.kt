package io.github.vladimirmi.internetradioplayer.presentation.player

import android.support.v4.media.MediaMetadataCompat
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface PlayerView : BaseView {

    fun setMedia(media: Media)

    fun setFavorite(isFavorite: Boolean)

    fun setMetadata(metadata: MediaMetadataCompat)

    fun showPaused()

    fun showBuffering()

    fun showPlaying()

    fun showPrevious()

    fun showNext()

    fun setGroup(group: String)

    fun openLinkDialog(url: String)

    fun openAddShortcutDialog()
}
