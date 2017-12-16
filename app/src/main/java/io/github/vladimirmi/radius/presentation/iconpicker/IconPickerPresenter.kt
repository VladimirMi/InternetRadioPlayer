package io.github.vladimirmi.radius.presentation.iconpicker

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

@InjectViewState
class IconPickerPresenter
@Inject constructor(private val repository: StationRepository)
    : BasePresenter<IconPickerView>() {

    override fun onFirstViewAttach() {
        val station = repository.current.value
        if (station.url.isBlank()) viewState.hideStationUrlOption()
        if (station.title.isBlank()) viewState.hideTextOption()
        viewState.option(true, false, false)
    }

    fun getStationUrlIcon() {
        viewState.setIconImage(repository.getStationIcon(repository.current.value.url))
    }

    fun setTextColor(colorInt: Int) {

    }

    fun setBackgroundColor(colorInt: Int) {
        viewState.setBackgroundColor(colorInt)
    }

    fun option(url: Boolean, name: Boolean, add: Boolean) {
        viewState.option(url, name, add)
    }
}