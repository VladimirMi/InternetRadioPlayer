package io.github.vladimirmi.internetradioplayer.presentation.root

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

interface RootView : BaseView {

    fun checkIntent()

    fun showLoadingIndicator(visible: Boolean)

    fun hidePlayer()

    fun collapsePlayer()

    fun expandPlayer()

    fun createStation(uri: Uri, addToFavorite: Boolean, startPlay: Boolean)

    fun setOffset(offset: Float)
}
