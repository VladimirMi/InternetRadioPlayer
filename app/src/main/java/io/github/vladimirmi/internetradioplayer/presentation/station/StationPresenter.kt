package io.github.vladimirmi.internetradioplayer.presentation.station

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ValidationException
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.root.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
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
            R.string.menu_station_delete -> viewState.openRemoveDialog()
            R.string.menu_station_shortcut -> addShortcut()
        }
    }

    private val editItem = MenuItemHolder(R.string.menu_station_edit, R.drawable.ic_edit, order = 0)
    private val saveItem = MenuItemHolder(R.string.menu_station_save, R.drawable.ic_submit, order = 0, showAsAction = true)

    private val toolbarBuilder = ToolbarBuilder().setToolbarTitle(interactor.currentStation.name)
            .addMenuItem(MenuItemHolder(R.string.menu_station_shortcut, R.drawable.ic_shortcut, order = 1))
            .addMenuItem(MenuItemHolder(R.string.menu_station_delete, R.drawable.ic_delete, order = 2))
            .setMenuActions(menuActions)

    override fun onFirstViewAttach() {
        viewState.setStation(interactor.currentStation)
        viewState.setGroup(interactor.getCurrentGroup())
        interactor.getCurrentGenres()
                .ioToMain()
                .subscribeBy { viewState.setGenres(it) }
                .addTo(compDisp)

        if (editMode) editMode() else viewMode()
    }

    fun removeStation() {
        interactor.removeStation(interactor.currentStation.id)
                .ioToMain()
                .subscribe {
                    controlsInteractor.stop()
                    if (interactor.haveStations()) router.exit()
                    else router.newRootScreen(Router.GET_STARTED_SCREEN)
                }
                .addTo(compDisp)
    }

    fun edit(stationInfo: StationInfo) {
        interactor.updateCurrentStation(stationInfo.stationName, stationInfo.groupName)
                .ioToMain()
                .subscribeBy(
                        onComplete = { viewMode() },
                        onError = {
                            if (it is ValidationException) viewState.showToast(it.resId)
                            else Timber.e(it)
                        })
                .addTo(compDisp)
    }

    fun cancelEdit() {
        interactor.currentStation = interactor.previousWhenEdit!!
        viewState.setStation(interactor.currentStation)
        viewMode()
    }

    fun create(stationInfo: StationInfo) {
        interactor.addCurrentStation(stationInfo.stationName, stationInfo.groupName)
                .ioToMain()
                .subscribeBy(
                        onComplete = {
                            viewState.showToast(R.string.toast_add_success)
                            viewMode()
                            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
                        },
                        onError = {
                            if (it is ValidationException) viewState.showToast(it.resId)
                            else Timber.e(it)
                        })
                .addTo(compDisp)
    }

    fun cancelCreate() {
        interactor.currentStation = interactor.previousWhenEdit!!
        viewMode()
        router.exit()
    }


    fun onBackPressed(): Boolean {
        when {
            interactor.createMode -> viewState.openCancelCreateDialog()
            editMode -> viewState.cancelEdit()
            else -> router.backTo(null)
        }
        return true
    }

    fun openLink(url: String) {
        if (!editMode) viewState.openLinkDialog(url)
    }

    private fun viewMode() {
        editMode = false
        viewState.setEditMode(editMode)
        val toolbar = toolbarBuilder.removeMenuItem(saveItem)
                .addMenuItem(editItem)

        viewState.buildToolbar(toolbar)
        controlsInteractor.editMode(false)
    }

    private fun editMode() {
        editMode = true
        viewState.setEditMode(editMode)
        val toolbar = toolbarBuilder.removeMenuItem(editItem)
                .addMenuItem(saveItem)

        viewState.buildToolbar(toolbar)
        controlsInteractor.editMode(true)
    }

    private fun changeMode() {
        when {
            interactor.createMode -> viewState.createStation()
            editMode -> viewState.editStation()
            else -> editMode()
        }
    }

    private fun addShortcut() {
        if (interactor.addCurrentShortcut()) {
            viewState.showToast(R.string.toast_add_shortcut_success)
        }
    }

    fun tryCancelEdit(stationInfo: StationInfo) {
        if (interactor.stationChanged(stationInfo)) {
            viewState.openCancelEditDialog()
        } else {
            cancelEdit()
        }
    }
}
