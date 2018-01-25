package io.github.vladimirmi.internetradioplayer.ui.base

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

open class BaseDialog(layoutId: Int, viewGroup: ViewGroup) {

    protected val dialogView: View by lazy {
        LayoutInflater.from(viewGroup.context).inflate(layoutId, null, false)
    }

    protected val dialog: AlertDialog by lazy {
        AlertDialog.Builder(viewGroup.context)
                .setView(dialogView)
                .create()
    }

    protected fun setPositiveAction(listener: DialogInterface.OnClickListener): BaseDialog {
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", listener)
        return this
    }

    protected fun setNegativeAction(listener: () -> Unit): BaseDialog {
        dialog.setOnCancelListener { listener.invoke() }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", { _, _ -> listener.invoke() })
        return this
    }

    fun show() = dialog.show()

    fun hide() = dialog.hide()

    fun dismiss() = dialog.dismiss()
}