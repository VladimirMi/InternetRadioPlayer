package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : MvpView {

    fun setStation(station: Station)

    fun buildToolbar(builder: ToolbarBuilder)

    fun setEditable(editable: Boolean)

    fun openEditDialog()

    fun closeEditDialog()

    fun openDeleteDialog()

    fun closeDeleteDialog()

    fun openLinkDialog(url: String)

    fun closeLinkDialog()
}