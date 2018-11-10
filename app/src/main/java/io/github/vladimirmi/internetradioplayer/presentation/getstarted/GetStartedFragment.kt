package io.github.vladimirmi.internetradioplayer.presentation.getstarted

import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.root.RootView
import kotlinx.android.synthetic.main.fragment_getstarted.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class GetStartedFragment : BaseFragment<GetStartedPresenter, GetStartedView>(), GetStartedView {

    override val layout = R.layout.fragment_getstarted

    override fun providePresenter(): GetStartedPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(GetStartedPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        addNewBt.setOnClickListener { openAddStationDialog() }
    }

    override fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }

    override fun showControls(visible: Boolean) {
        (activity as RootView).showControls(visible)
    }
}
