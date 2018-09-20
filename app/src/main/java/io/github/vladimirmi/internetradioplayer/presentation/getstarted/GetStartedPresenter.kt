package io.github.vladimirmi.internetradioplayer.presentation.getstarted

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.root.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */


@InjectViewState
class GetStartedPresenter
@Inject constructor() : BasePresenter<GetStartedView>() {

    private val actions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.string.menu_add_station -> viewState.openAddStationDialog()
        }
    }

    private val builder = ToolbarBuilder().addMenuItem(MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add))
            .setMenuActions(actions)

    override fun onFirstViewAttach() {
        viewState.buildToolbar(builder)
    }
}
