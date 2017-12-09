package io.github.vladimirmi.radius.presentation.metadata

import com.arellomobile.mvp.MvpView

/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

interface MetadataView : MvpView {

    fun setInfo(string: String)

    fun hide()
}