package io.github.vladimirmi.internetradioplayer.presentation.getstarted

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.base.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */


class GetStartedPresenter
@Inject constructor(private val interactor: StationInteractor) : BasePresenter<GetStartedView>() {

    private val builder = ToolbarBuilder.standard()
            .addMenuItem(MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add))
            .setMenuActions {
                if (it.itemId == R.string.menu_add_station) view?.openAddStationDialog()
            }

    override fun onFirstAttach(view: GetStartedView) {
        view.buildToolbar(builder)
    }

    override fun onAttach(view: GetStartedView) {
        view.showControls(interactor.haveStations())
    }
}
