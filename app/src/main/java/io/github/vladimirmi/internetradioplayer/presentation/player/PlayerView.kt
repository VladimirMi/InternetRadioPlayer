package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface PlayerView : BaseView {

    fun setStation(station: Station)

    fun setEditMode(editMode: Boolean)

    fun openLinkDialog(url: String)

    fun openAddShortcutDialog()
}
