package io.github.vladimirmi.internetradioplayer.presentation.main

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseDialogFragment
import io.github.vladimirmi.internetradioplayer.presentation.root.RootView
import kotlinx.android.synthetic.main.dialog_new_station.view.*

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class NewStationDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_new_station)
    }

    @SuppressLint("InflateParams")
    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_new_station, null)
    }

    override fun onPositive() {
        dialogView?.let {
            val uri = Uri.parse(it.linkEt.text.toString())
            (activity as? RootView)?.createStation(uri, name = null, addToFavorite = true, startPlay = false)
        }
    }

    override fun onNegative() {
    }
}
