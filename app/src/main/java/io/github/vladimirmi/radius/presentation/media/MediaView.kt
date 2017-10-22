package io.github.vladimirmi.radius.presentation.media

import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.data.entity.Media

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface MediaView : MvpView {
    fun setMediaList(mediaList: List<Media>)
}