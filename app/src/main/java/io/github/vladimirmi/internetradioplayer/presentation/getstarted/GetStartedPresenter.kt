package io.github.vladimirmi.internetradioplayer.presentation.getstarted

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.base.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */


@InjectViewState
class GetStartedPresenter
@Inject constructor() : BasePresenter<GetStartedView>() {

    private val builder = ToolbarBuilder.standart()
            .addMenuItem(MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add))
            .setMenuActions {
                if (it.itemId == R.string.menu_add_station) viewState.openAddStationDialog()
            }

    override fun onFirstViewAttach() {
        viewState.buildToolbar(builder)
    }
}
