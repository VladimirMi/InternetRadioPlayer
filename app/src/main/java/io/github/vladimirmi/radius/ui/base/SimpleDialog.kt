package io.github.vladimirmi.radius.ui.base

import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import kotlinx.android.synthetic.main.dialog_simple.view.*

/**
 * Created by Vladimir Mikhalev 30.11.2017.
 */

class SimpleDialog(viewGroup: ViewGroup) : BaseDialog(R.layout.dialog_simple, viewGroup) {


    fun setMessage(string: String): SimpleDialog {
        dialogView.dialog_message.text = string
        return this
    }

    fun setPositiveAction(action: () -> Unit): SimpleDialog {
        dialogView.ok.setOnClickListener { action() }
        return this
    }

    fun setNegativeAction(action: () -> Unit): SimpleDialog {
        dialogView.cancel.setOnClickListener { action() }
        return this
    }

    fun setCancelable(cancelable: Boolean): SimpleDialog {
        dialog.setCancelable(cancelable)
        return this
    }

    override fun dismiss() {
        dialogView.ok.setOnClickListener(null)
        dialogView.cancel.setOnClickListener(null)
        super.dismiss()
    }
}