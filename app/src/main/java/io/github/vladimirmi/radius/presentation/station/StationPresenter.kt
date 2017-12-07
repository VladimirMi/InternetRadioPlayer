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

    lateinit var id: String
    private var editMode = false
    private var createMode = false
    private lateinit var prevSelectedStation: Station

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_station_edit -> editMode()
            R.id.menu_station_delete -> viewState.openDeleteDialog()
            R.id.menu_station_save -> viewState.openSaveDialog()
        }
    }

    private val toolbarBuilder: ToolbarBuilder
        get() {
            val station = if (createMode) repository.newStation else repository.getStation(id)
            return ToolbarBuilder().setToolbarTitle(station!!.title)
        }


    override fun onFirstViewAttach() {
        viewState.setStation(repository.newStation ?: repository.getStation(id))
        if (repository.newStation != null) createMode() else viewMode()
    }

    fun viewMode() {
        editMode = false
        createMode = false
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station
        ))

        viewState.buildToolbar(toolbar)
        viewState.setEditMode(false)
    }

    private fun createMode() {
        createMode = true
        prevSelectedStation = repository.selected.value
        repository.selected.accept(repository.newStation)
        editMode()
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
            repository.remove(repository.getStation(id))
            repository.next()
            router.exit()
        }
    }


    fun edit(station: Station?) {
        viewState.closeSaveDialog()
        if (station != null) {
            val old = repository.getStation(id)
            if (old.path != station.path) {
                repository.remove(old)
                repository.add(station)
            } else if (old != station) {
                repository.update(station)
            }
            viewMode()
        }
    }

    fun cancelEdit(cancel: Boolean) {
        viewState.closeCancelEditDialog()
        if (cancel) {
            viewState.setStation(repository.getStation(id))
            viewMode()
        }
    }

    fun create(station: Station?) {
        viewState.closeCreateDialog()
        if (station != null) {
            if (repository.add(station)) {
                viewState.showToast(R.string.toast_add_success)
                repository.newStation = null
            } else {
                viewState.showToast(R.string.toast_add_force)
            }
        }
    }

    fun cancelCreate(cancel: Boolean) {
        viewState.closeCancelCreateDialog()
        if (cancel) {
            repository.newStation = null
            repository.setSelected(prevSelectedStation)
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

    fun isChanged(station: Station) = station != repository.getStation(id)

    fun openLink(url: String) {
        if (!editMode) viewState.openLinkDialog(url)
    }

    fun cancelLink() {
        viewState.closeLinkDialog()
    }
}