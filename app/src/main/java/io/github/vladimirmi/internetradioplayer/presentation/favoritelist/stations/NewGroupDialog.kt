package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_new_group.view.*

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

class NewGroupDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_new_group)
    }

    @SuppressLint("InflateParams")
    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_new_group, null)
    }

    override fun onPositive() {
        (parentFragment as? Callback)?.onGroupCreate(dialogView!!.groupEt.text.toString())
    }

    override fun onNegative() {
        (parentFragment as? Callback)?.onCancelGroupCreate()
    }

    interface Callback {
        fun onGroupCreate(group: String)

        fun onCancelGroupCreate()
    }
}
