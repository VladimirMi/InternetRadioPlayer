package io.github.vladimirmi.internetradioplayer.presentation.station

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.root.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
class StationPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenter<StationView>() {

    private var editMode = false
    private var createMode: Boolean
        get() = stationInteractor.isCreateMode
        set(value) {
            stationInteractor.isCreateMode = value
        }

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.string.menu_station_edit -> editMode()
            R.string.menu_station_delete -> viewState.openRemoveDialog()
            R.string.menu_station_save -> viewState.editStation()
            R.string.menu_station_shortcut -> addShortcut()
        }
    }

    private val toolbarBuilder = ToolbarBuilder().setToolbarTitle(stationInteractor.currentStation.name)
            .addMenuItem(MenuItemHolder(R.string.menu_station_edit, R.drawable.ic_edit))
            .addMenuItem(MenuItemHolder(R.string.menu_station_shortcut, R.drawable.ic_shortcut))
            .addMenuItem(MenuItemHolder(R.string.menu_station_delete, R.drawable.ic_delete))
            .setMenuActions(menuActions)

    override fun onFirstViewAttach() {
        viewState.setStation(stationInteractor.currentStation)
        if (createMode) editMode() else viewMode()
    }

    override fun attachView(view: StationView?) {
        super.attachView(view)
        viewState.setStationIcon(stationInteractor.currentIcon.bitmap)
    }

    private fun viewMode() {
        editMode = false
        viewState.setEditMode(editMode)
        val toolbar = toolbarBuilder.replaceMenuItem(R.string.menu_station_save,
                MenuItemHolder(R.string.menu_station_edit, R.drawable.ic_edit))

        viewState.buildToolbar(toolbar)
        controlsInteractor.tryEnableNextPrevious(true)
    }

    private fun editMode() {
        editMode = true
        viewState.setEditMode(editMode)
        val toolbar = toolbarBuilder.replaceMenuItem(R.string.menu_station_edit,
                MenuItemHolder(R.string.menu_station_save, R.drawable.ic_submit))

        viewState.buildToolbar(toolbar)
        controlsInteractor.tryEnableNextPrevious(false)
    }

    fun changeMode() {
        when {
            createMode -> viewState.createStation()
            editMode -> viewState.editStation()
            else -> editMode()
        }
    }

    fun removeStation() {
        val station = stationInteractor.currentStation
        val pos = stationInteractor.stationList.positionOfFirst { it.id == station.id }
        if (pos == 0) {
            controlsInteractor.nextStation()
        } else {
            controlsInteractor.previousStation()
        }
        stationInteractor.removeStation(station)
                .subscribe {
                    stationInteractor.removeShortcut(station)
                    router.exit()
                }
                .addTo(compDisp)
    }


    fun edit(station: Station) {
        stationInteractor.updateCurrentStation(station.copy(favorite = stationInteractor.currentStation.favorite))
                .subscribe { viewMode() }
                .addTo(compDisp)
    }

    fun cancelEdit() {
        viewState.setStation(stationInteractor.currentStation)
        stationInteractor.currentStation = stationInteractor.currentStation
        viewMode()
    }

    fun create(station: Station) {
        stationInteractor.addStation(station)
                .ioToMain()
                .subscribeBy { added ->
                    if (added) {
                        viewState.showToast(R.string.toast_add_success)
                        createMode = false
                        router.newRootScreen(Router.MEDIA_LIST_SCREEN)
                    } else {
                        viewState.showToast(R.string.toast_add_force)
                    }
                }
                .addTo(compDisp)
    }

    fun cancelCreate() {
        stationInteractor.previousWhenCreate?.let { stationInteractor.currentStation = it }
        controlsInteractor.tryEnableNextPrevious(true)
        createMode = false
        router.exit()
    }


    fun onBackPressed(): Boolean {
        when {
            createMode -> viewState.openCancelCreateDialog()
            editMode -> {
                stationInteractor.iconChanged()
                        .subscribeBy { viewState.openCancelEditDialog(stationInteractor.currentStation, it) }
                        .addTo(compDisp)
            }
            else -> router.backTo(null)
        }
        return true
    }

    fun openLink(url: String) {
        if (!editMode) viewState.openLinkDialog(url)
    }

    fun changeIcon() {
        router.navigateTo(Router.ICON_PICKER_SCREEN)
    }

    private fun addShortcut() {
        if (stationInteractor.addCurrentShortcut()) {
            viewState.showToast(R.string.toast_add_shortcut_success)
        }
    }
}