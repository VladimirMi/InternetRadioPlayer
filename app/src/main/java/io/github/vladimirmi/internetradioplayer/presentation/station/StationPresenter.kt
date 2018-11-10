package io.github.vladimirmi.internetradioplayer.presentation.station

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.subscribeByEx
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.base.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class StationPresenter
@Inject constructor(private val interactor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenter<StationView>() {

    private var editMode: Boolean
        get() = interactor.previousWhenEdit != null
        set(value) = interactor.setEditMode(value)

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.string.menu_station_edit, R.string.menu_station_save -> changeMode()
            R.string.menu_station_delete -> view?.openRemoveDialog()
            R.string.menu_station_shortcut -> view?.openAddShortcutDialog()
        }
    }

    private val editItem = MenuItemHolder(R.string.menu_station_edit, R.drawable.ic_edit, order = 0)
    private val saveItem = MenuItemHolder(R.string.menu_station_save, R.drawable.ic_submit, order = 0, showAsAction = true)
    private val deleteItem = MenuItemHolder(R.string.menu_station_delete, R.drawable.ic_delete, order = 2)

    private val toolbarBuilder = ToolbarBuilder.standard()
            .setToolbarTitle(interactor.currentStation.name)
            .addMenuItem(MenuItemHolder(R.string.menu_station_shortcut, R.drawable.ic_shortcut, order = 1))
            .setMenuActions(menuActions)

    override fun onAttach(view: StationView) {
        view.setStation(interactor.currentStation)

        if (editMode) editMode() else viewMode()
    }

    fun removeStation() {
        interactor.removeStation(interactor.currentStation.id)
                .ioToMain()
                .subscribeByEx(onComplete = {
                    controlsInteractor.stop()
                    if (interactor.haveStations()) router.exit()
                    else router.newRootScreen(Router.GET_STARTED_SCREEN)
                })
                .addTo(dataSubs)
    }

    fun edit(stationInfo: StationInfo) {
        interactor.updateCurrentStation(stationInfo.stationName, stationInfo.groupName)
                .ioToMain()
                .subscribeByEx(onComplete = { viewMode() })
                .addTo(viewSubs)
    }

    fun cancelEdit() {
        interactor.currentStation = interactor.previousWhenEdit!!
        view?.setStation(interactor.currentStation)
        viewMode()
    }

    fun create(stationInfo: StationInfo) {
        interactor.addCurrentStation(stationInfo.stationName, stationInfo.groupName)
                .ioToMain()
                .subscribeByEx(
                        onComplete = {
                            view?.showToast(R.string.toast_add_success)
                            viewMode()
                            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
                        })
                .addTo(viewSubs)
    }

    fun cancelCreate() {
        interactor.currentStation = interactor.previousWhenEdit!!
        viewMode()
        router.exit()
    }

    fun onBackPressed(): Boolean {
        when {
            interactor.createMode -> view?.openCancelCreateDialog()
            editMode -> view?.cancelEdit()
            else -> router.backTo(null)
        }
        return true
    }

    fun openLink(url: String) {
        if (!editMode) view?.openLinkDialog(url)
    }

    private fun viewMode() {
        editMode = false
        view?.setEditMode(editMode)
        val toolbar = toolbarBuilder
                .removeMenuItem(saveItem)
                .addMenuItem(editItem)
                .addMenuItem(deleteItem)

        view?.buildToolbar(toolbar)
        controlsInteractor.editMode(false)
    }

    private fun editMode() {
        if (!interactor.createMode) editMode = true
        view?.setEditMode(editMode)
        val toolbar = toolbarBuilder
                .removeMenuItem(editItem)
                .addMenuItem(saveItem)
                .removeMenuItem(deleteItem)

        view?.buildToolbar(toolbar)
        controlsInteractor.editMode(true)
    }

    private fun changeMode() {
        when {
            interactor.createMode -> view?.createStation()
            editMode -> view?.editStation()
            else -> editMode()
        }
    }

    fun addShortcut(startPlay: Boolean) {
        if (interactor.addCurrentShortcut(startPlay)) {
            view?.showToast(R.string.toast_add_shortcut_success)
        }
    }

    fun tryCancelEdit(stationInfo: StationInfo) {
        if (interactor.stationChanged(stationInfo)) {
            view?.openCancelEditDialog()
        } else {
            cancelEdit()
        }
    }
}
