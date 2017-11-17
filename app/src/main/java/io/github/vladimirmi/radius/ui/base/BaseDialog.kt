package io.github.vladimirmi.radius.ui.base

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

open class BaseDialog(layoutId: Int, viewGroup: ViewGroup) {

    val dialogView: View by lazy {
        LayoutInflater.from(viewGroup.context).inflate(layoutId, null, false)
    }

    protected val dialog: AlertDialog by lazy {
        AlertDialog.Builder(viewGroup.context)
                .setView(dialogView)
                .create()
    }

    open fun open() = dialog.show()

    open fun hide() = dialog.hide()

    open fun close() = dialog.dismiss()
}