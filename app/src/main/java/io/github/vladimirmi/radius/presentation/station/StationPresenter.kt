package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
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
        initToolbar()
        viewState.setStation(station)
    }

    private fun initToolbar() {
        val builder = ToolbarBuilder()
                .addAction(MenuItemHolder(itemTitle = "edit",
                        iconResId = R.drawable.ic_edit,
                        actions = { edit() }))

        viewState.buildToolbar(builder)
        viewState.setEditable(false)
    }

    private fun edit() {
        val builder = ToolbarBuilder().addAction(
                MenuItemHolder(itemTitle = "submit",
                        iconResId = R.drawable.ic_submit,
                        actions = { submit() }))

        viewState.buildToolbar(builder)
        viewState.setEditable(true)
    }

    fun submit() {
        initToolbar()
    }
}