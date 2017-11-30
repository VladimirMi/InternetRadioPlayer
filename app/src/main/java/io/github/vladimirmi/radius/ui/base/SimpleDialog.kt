package io.github.vladimirmi.radius.ui.base

import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import kotlinx.android.synthetic.main.dialog_simple.view.*

/**
 * Created by Vladimir Mikhalev 30.11.2017.
 */

class SimpleDialog<T>(viewGroup: ViewGroup) : BaseDialog(R.layout.dialog_simple, viewGroup) {

    private var obj: T? = null

    fun setObject(o: T): SimpleDialog<T> {
        obj = o
        return this
    }

    fun setMessage(string: String): SimpleDialog<T> {
        dialogView.dialog_message.text = string
        return this
    }

    fun setPositiveAction(action: (T) -> Unit): SimpleDialog<T> {
        dialogView.ok.setOnClickListener { obj?.let { it1 -> action(it1) } }
        return this
    }

    fun setNegativeAction(action: (T) -> Unit): SimpleDialog<T> {
        dialogView.cancel.setOnClickListener {
            obj?.let { it1 -> action(it1) }
        }
        return this
    }

    fun setCancelable(cancelable: Boolean): SimpleDialog<T> {
        dialog.setCancelable(cancelable)
        return this
    }
}