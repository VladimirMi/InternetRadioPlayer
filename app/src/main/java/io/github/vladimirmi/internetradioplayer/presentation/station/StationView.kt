package io.github.vladimirmi.internetradioplayer.presentation.station

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : BaseView {

    fun setStation(station: Station)

    fun setEditMode(editMode: Boolean)

    fun editStation()

    fun createStation()

    fun openRemoveDialog()

    fun openLinkDialog(url: String)

    fun openCancelEditDialog()

    fun openCancelCreateDialog()

    fun openAddShortcutDialog()

    fun cancelEdit()
}
