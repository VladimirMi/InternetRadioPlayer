package io.github.vladimirmi.radius.ui.dialogs

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.dialogs.RemoveStationPresenter
import io.github.vladimirmi.radius.presentation.dialogs.RemoveStationView
import io.github.vladimirmi.radius.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_simple.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class RemoveStationDialog : BaseDialogFragment(), RemoveStationView {
    override val layoutRes = R.layout.dialog_simple

    companion object {
        fun newInstance(station: Station): RemoveStationDialog {
            return RemoveStationDialog().apply {
                arguments = Bundle().apply { putString("id", station.id) }
            }
        }
    }

    @InjectPresenter lateinit var presenter: RemoveStationPresenter

    @ProvidePresenter
    fun providePresenter(): RemoveStationPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(RemoveStationPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        isCancelable = false
        presenter.id = arguments.getString("id")
        ok.setOnClickListener { presenter.ok() }
        cancel.setOnClickListener { presenter.cancel() }
        dialog_message.setText(R.string.remove_message)
    }

    override fun close() = dismiss()
}