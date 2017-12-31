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

    fun openRemoveDialog()

    fun openLinkDialog(url: String)

    fun openCancelEditDialog()

    fun openCancelCreateDialog()

    fun showToast(resId: Int)
}