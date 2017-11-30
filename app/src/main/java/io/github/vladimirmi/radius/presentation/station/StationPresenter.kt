package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.presentation.root.MenuItemHolder
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
class StationPresenter
@Inject constructor(private val repository: StationRepository)
    : BasePresenter<StationView>() {

    lateinit var id: String

    override fun onFirstViewAttach() {
        val station = repository.getStation(id)
        viewState.setStation(station)
        viewMode()
    }

    private fun viewMode() {
        val builder = ToolbarBuilder()
                .addAction(MenuItemHolder(itemTitle = "editMode",
                        iconResId = R.drawable.ic_edit,
                        actions = { editMode() }))

        viewState.buildToolbar(builder)
        viewState.setEditable(false)
    }

    private fun editMode() {
        val builder = ToolbarBuilder().addAction(
                MenuItemHolder(itemTitle = "submit",
                        iconResId = R.drawable.ic_submit,
                        actions = { viewState.openSubmitEditDialog() }))

        viewState.buildToolbar(builder)
        viewState.setEditable(true)
    }

    fun submitEdit(station: Station) {
        val old = repository.getStation(id)
        if (old.path != station.path) {
            repository.remove(old)
            repository.add(station)
        } else if (old != station) {
            repository.update(station)
        }

        viewState.closeSubmitEditDialog()
        viewMode()
    }

    fun cancelEdit() {
        viewState.closeSubmitEditDialog()
    }
}