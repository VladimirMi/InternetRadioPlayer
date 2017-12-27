package io.github.vladimirmi.radius.presentation.getstarted

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_getstarted.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class GetStartedFragment : BaseFragment(), GetStartedView {

    override val layoutRes = R.layout.fragment_getstarted

    @InjectPresenter lateinit var presenter: GetStartedPresenter

    @ProvidePresenter
    fun providePresenter(): GetStartedPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(GetStartedPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        addNewBt.setOnClickListener {
            NewStationDialogFragment().show(childFragmentManager, "new_station_dialog")
        }
    }

    fun addStation(uri: Uri) {
        presenter.addStation(uri)
    }
}