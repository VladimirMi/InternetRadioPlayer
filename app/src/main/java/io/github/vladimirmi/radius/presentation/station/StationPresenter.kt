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

    private val menuActions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.id.menu_station_edit -> editMode()
            R.id.menu_station_delete -> viewState.openDeleteDialog()
            R.id.menu_station_save -> viewState.openSaveDialog()
        }
    }

    private val toolbarBuilder
        get() = ToolbarBuilder().setToolbarTitle(repository.getStation(id).title)

    override fun onFirstViewAttach() {
        viewState.setStation(repository.getStation(id))
        viewMode()
    }

    fun viewMode() {
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station
        ))

        viewState.buildToolbar(toolbar)
        viewState.setEditMode(false)
        editMode = false
    }

    private fun editMode() {
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(
                itemTitle = "more",
                iconResId = R.drawable.ic_more,
                actions = menuActions,
                popupMenu = R.menu.menu_station_edit
        ))

        viewState.buildToolbar(toolbar)
        viewState.setEditMode(true)
        editMode = true
    }

    fun changeMode() {
        if (editMode) {
            viewState.openSaveDialog()
        } else {
            editMode()
        }
    }

    fun delete() {
        repository.remove(repository.getStation(id))
        repository.next()
        viewState.closeDeleteDialog()
        router.exit()
    }

    fun cancelDelete() {
        viewState.closeDeleteDialog()

    }

    fun edit(station: Station) {
        val old = repository.getStation(id)
        if (old.path != station.path) {
            repository.remove(old)
            repository.add(station)
        } else if (old != station) {
            repository.update(station)
        }
        viewState.closeSaveDialog()
        viewMode()
    }

    fun cancelEdit() {
        viewState.closeSaveDialog()
        viewState.setStation(repository.getStation(id))
        viewMode()
    }

    fun onBackPressed(): Boolean {
        if (editMode) {
            viewState.openSaveDialog()
        } else {
            router.backTo(Router.MEDIA_LIST_SCREEN)
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