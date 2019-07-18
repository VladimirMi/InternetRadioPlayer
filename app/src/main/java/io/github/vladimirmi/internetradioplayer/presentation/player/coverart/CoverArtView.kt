package io.github.vladimirmi.internetradioplayer.presentation.player.coverart

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 28.03.2019.
 */

interface CoverArtView : BaseView {

    fun setCoverArt(uri: String)
}