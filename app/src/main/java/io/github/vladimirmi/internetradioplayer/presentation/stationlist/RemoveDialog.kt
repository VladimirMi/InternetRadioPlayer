package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.os.Bundle
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.ui.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 28.12.2017.
 */

open class RemoveDialog : BaseDialogFragment() {

    companion object {
        private const val KEY_STATION = "KEY_STATION"

        fun newInstance(station: Station): RemoveDialog {
            val args = Bundle().apply { putParcelable(KEY_STATION, station) }
            return RemoveDialog().apply { arguments = args }
        }
    }

    override fun getTitle(): String {
        return getString(R.string.dialog_remove_message)
    }

    override fun onPositive() {
        val station = arguments.getParcelable<Station>(KEY_STATION)
        (parentFragment as StationListFragment).presenter
                .removeStation(station)
    }

    override fun onNegative() {
        (parentFragment as StationListFragment).notifyList()
    }
}
