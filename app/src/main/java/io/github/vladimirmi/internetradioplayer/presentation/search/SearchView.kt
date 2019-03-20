package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

interface SearchView : BaseView {

    fun showPage(position: Int)

    fun selectTab(position: Int)
}