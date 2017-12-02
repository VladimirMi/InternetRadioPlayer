package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.presentation.root.MenuItemHolder
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import ru.terrakok.cicerone.Router
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
    private val toolbarBuilder
        get() = ToolbarBuilder()
                //todo rename to allow without arg
                .setBackNavigationEnabled(true)
                .addAction(MenuItemHolder(itemTitle = "delete",
                        iconResId = R.drawable.ic_delete,
                        actions = { viewState.openDeleteDialog() }))

    override fun onFirstViewAttach() {
        val station = repository.getStation(id)
        viewState.setStation(station)
        viewMode()
    }

    private fun viewMode() {
        val toolbar = toolbarBuilder.addAction(MenuItemHolder(itemTitle = "editMode",
                iconResId = R.drawable.ic_edit,
                actions = { editMode() }))

        viewState.buildToolbar(toolbar)
        viewState.setEditable(false)
    }

    private fun editMode() {
        val toolbar = toolbarBuilder.addAction(
                MenuItemHolder(itemTitle = "submit",
                        iconResId = R.drawable.ic_submit,
                        actions = { viewState.openEditDialog() }))

        viewState.buildToolbar(toolbar)
        viewState.setEditable(true)
    }

    fun delete() {
        repository.remove(repository.getStation(id))
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

        viewState.closeEditDialog()
        viewMode()
    }

    fun cancelEdit() {
        viewState.closeEditDialog()
    }
}