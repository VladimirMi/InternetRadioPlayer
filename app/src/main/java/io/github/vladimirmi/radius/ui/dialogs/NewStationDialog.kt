package io.github.vladimirmi.radius.ui.dialogs

import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.ui.base.ValidationDialog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_new_station.view.*

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

class NewStationDialog(viewGroup: ViewGroup, submit: (Station) -> Unit)
    : ValidationDialog(R.layout.dialog_new_station, viewGroup) {

    private val name = dialogView.name
    private val group = dialogView.group
    private val ok = dialogView.ok
    private val cancel = dialogView.cancel

    lateinit var station: Station

    init {
        dialog.setCanceledOnTouchOutside(false)
        cancel.setOnClickListener { dismiss() }
        ok.setOnClickListener {
            val request = station.copy(
                    title = name.text.toString(),
                    group = group.text.toString())
            submit(request)
        }
    }

    override fun listenFields(): Disposable {
        val nameObs = name.validate(NAME_PATTERN, dialogView.name_error,
                dialogView.context.getString(R.string.dialog_add_name_err))

        return validateForm(listOf(nameObs))
                .subscribe { ok.isEnabled = it }
    }

    fun setupDialog(station: Station) {
        this.station = station
        setupDialog()
    }

    override fun setupDialog() {
        name.setText(station.title)
        group.setText(station.group)
        name.setSelection(station.title.length)
    }

    override fun clearDialog() {
        name.setText("")
        group.setText("")
    }
}