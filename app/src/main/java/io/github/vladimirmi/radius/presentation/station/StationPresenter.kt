package io.github.vladimirmi.radius.presentation.station

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
        editMode = false
    }

    private fun editMode() {
        val toolbar = toolbarBuilder.addAction(
                MenuItemHolder(itemTitle = "submit",
                        iconResId = R.drawable.ic_submit,
                        actions = { viewState.openEditDialog() }))

        viewState.buildToolbar(toolbar)
        viewState.setEditable(true)
        editMode = true
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

        viewState.closeEditDialog()
        viewMode()
    }

    fun cancelEdit() {
        viewMode()
        viewState.closeEditDialog()
    }

    fun onBackPressed(): Boolean {
        if (editMode) {
            viewState.openEditDialog()
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