package io.github.vladimirmi.internetradioplayer.presentation.getstarted

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenterLegacy
import io.github.vladimirmi.internetradioplayer.presentation.base.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */


@InjectViewState
class GetStartedPresenter
@Inject constructor(private val interactor: StationInteractor) : BasePresenterLegacy<GetStartedView>() {

    private val builder = ToolbarBuilder.standard()
            .addMenuItem(MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add))
            .setMenuActions {
                if (it.itemId == R.string.menu_add_station) viewState.openAddStationDialog()
            }

    override fun onFirstViewAttach() {
        viewState.buildToolbar(builder)
    }

    override fun attachView(view: GetStartedView?) {
        super.attachView(view)
        viewState.showControls(interactor.haveStations())
    }
}
