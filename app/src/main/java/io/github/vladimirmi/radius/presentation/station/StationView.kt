package io.github.vladimirmi.radius.presentation.station

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : MvpView {

    fun setStation(station: Station)

    fun setStationIcon(icon: Bitmap)

    fun buildToolbar(builder: ToolbarBuilder)

    fun setEditMode(editMode: Boolean)

    fun editStation()

    fun createStation()

    fun openDeleteDialog()

    fun closeDeleteDialog()

    fun openLinkDialog(url: String)

    fun closeLinkDialog()

    fun openCancelEditDialog()

    fun closeCancelEditDialog()

    fun openCancelCreateDialog()

    fun closeCancelCreateDialog()

    fun showToast(resId: Int)
}