package io.github.vladimirmi.radius.presentation.station

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.ioToMain
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.interactor.IconInteractor
import io.github.vladimirmi.radius.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.MenuItemHolder
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
class StationPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val iconInteractor: IconInteractor,
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
            R.id.menu_station_edit -> editMode()
            R.id.menu_station_delete -> viewState.openDeleteDialog()
            R.id.menu_station_save -> viewState.editStation()
        }
    }

    private val toolbarBuilder: ToolbarBuilder
        get() = ToolbarBuilder().setToolbarTitle(stationInteractor.currentStation.title)

    override fun onFirstViewAttach() {
        viewState.setStation(stationInteractor.currentStation)
        iconInteractor.getCurrentIcon()
                .ioToMain()
                .subscribe { viewState.setStationIcon(it.bitmap) }
                .addTo(compDisp)

        createMode = stationInteractor.isCreateMode
        if (createMode) editMode() else viewMode()
    }

    private fun viewMode() {
        editMode = false
        viewState.setEditMode(editMode)
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station
        ))

        viewState.buildToolbar(toolbar)
        controlsInteractor.tryEnableNextPrevious(true)
    }

    private fun editMode() {
        editMode = true
        viewState.setEditMode(editMode)
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station_edit
        ))

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

    fun delete(delete: Boolean) {
        viewState.closeDeleteDialog()
        if (delete) {
            stationInteractor.removeStation(stationInteractor.currentStation)
            controlsInteractor.nextStation()
            router.exit()
        }
    }


    fun edit(station: Station) {
        stationInteractor.updateCurrentStation(station)
                .subscribe { viewMode() }
                .addTo(compDisp)
    }

    fun cancelEdit(cancel: Boolean) {
        viewState.closeCancelEditDialog()
        if (cancel) {
            viewState.setStation(stationInteractor.currentStation)
            viewMode()
        }
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

    fun cancelCreate(cancel: Boolean) {
        viewState.closeCancelCreateDialog()
        if (cancel) {
            createMode = false
//            previousStation?.let { stationInteractor.currentStation = it }
            router.exit()
        }
    }


    fun onBackPressed(): Boolean {
        when {
            createMode -> viewState.openCancelCreateDialog()
            editMode -> viewState.openCancelEditDialog()
            else -> router.backTo(Router.MEDIA_LIST_SCREEN)
        }
        return true
    }

    fun openLink(url: String) {
        if (!editMode) viewState.openLinkDialog(url)
    }

    fun cancelLink() {
        viewState.closeLinkDialog()
    }

    fun changeIcon() {
        router.navigateTo(Router.ICON_PICKER_SCREEN)
    }
}