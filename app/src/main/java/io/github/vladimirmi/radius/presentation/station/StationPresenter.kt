package io.github.vladimirmi.radius.presentation.station

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.MenuItemHolder
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
class StationPresenter
@Inject constructor(private val repository: StationRepository,
                    private val router: Router)
    : BasePresenter<StationView>() {

    val stationId get() = repository.currentStation.value.id
    private var previousStation: Station? = null
    private var editMode = false
    private var createMode = false

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_station_edit -> editMode()
            R.id.menu_station_delete -> viewState.openDeleteDialog()
            R.id.menu_station_save -> viewState.openSaveDialog()
        }
    }

    private val toolbarBuilder: ToolbarBuilder
        get() = ToolbarBuilder().setToolbarTitle(repository.currentStation.value.title)

    override fun onFirstViewAttach() {
        if (repository.newStation != null) {
            createMode = true
            if (repository.currentStation.hasValue()) {
                previousStation = repository.currentStation.value
            }
            repository.currentStation.accept(repository.newStation)
        }
        viewState.setStation(repository.currentStation.value)
        if (createMode) editMode() else viewMode()
    }

    override fun attachView(view: StationView?) {
        super.attachView(view)
        viewState.setStationIcon(repository.getStationIcon().blockingGet())
    }

    fun viewMode() {
        editMode = false
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station
        ))

        viewState.buildToolbar(toolbar)
        viewState.setEditMode(false)
    }

    private fun editMode() {
        editMode = true
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station_edit
        ))

        viewState.buildToolbar(toolbar)
        viewState.setEditMode(true)
    }

    fun changeMode() {
        when {
            createMode -> viewState.openCreateDialog()
            editMode -> viewState.openSaveDialog()
            else -> editMode()
        }
    }

    fun delete(delete: Boolean) {
        viewState.closeDeleteDialog()
        if (delete) {
            repository.removeStation(repository.currentStation.value)
            repository.nextStation()
            router.exit()
        }
    }


    fun edit(station: Station?) {
        viewState.closeSaveDialog()
        if (station != null) {
            repository.updateStation(station)
            viewMode()
        }
    }

    fun cancelEdit(cancel: Boolean) {
        viewState.closeCancelEditDialog()
        if (cancel) {
            viewState.setStation(repository.currentStation.value)
            viewMode()
        }
    }

    fun create(station: Station?) {
        viewState.closeCreateDialog()
        if (station != null) {
            if (repository.addStation(station)) {
                viewState.showToast(R.string.toast_add_success)
                repository.newStation = null
                viewMode()
            } else {
                viewState.showToast(R.string.toast_add_force)
            }
        }
    }

    fun cancelCreate(cancel: Boolean) {
        viewState.closeCancelCreateDialog()
        if (cancel) {
            repository.newStation = null
            previousStation?.let { repository.setCurrent(it) }
            router.backTo(Router.MEDIA_LIST_SCREEN)
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

    fun isChanged(station: Station) = station != repository.currentStation.value

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