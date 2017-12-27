package io.github.vladimirmi.radius.presentation.getstarted

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_new_station.view.*

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class NewStationDialogFragment : BaseDialogFragment() {

    override fun getTitle(): String? {
        return context.getString(R.string.dialog_new_station)
    }

    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_new_station, null)
    }

    override fun onPositive() {
        (parentFragment as GetStartedFragment).addStation(Uri.parse(dialogView!!.linkEt.text.toString()))
    }

    override fun onNegative() {
    }
}

