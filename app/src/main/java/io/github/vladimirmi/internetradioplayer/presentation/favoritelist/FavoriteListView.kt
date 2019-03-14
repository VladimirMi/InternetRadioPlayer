package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 25.02.2019.
 */

interface FavoriteListView: BaseView {

    fun showTabs(visible: Boolean)

    fun showPage(position: Int)

    fun selectTab(position: Int)
}