package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import android.graphics.Color
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

@InjectViewState
class IconPickerPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val repository: StationRepository,
                    private val router: Router)
    : BasePresenter<IconPickerView>() {

    private val station = repository.currentStation.value

    override fun onFirstViewAttach() {
        if (station.url.isBlank()) viewState.hideStationUrlOption()
        if (station.title.isBlank()) viewState.hideTextOption()
        viewState.option(true, false, false)
        viewState.setIconImage(repository.getStationIcon(station.uri).blockingGet())
        viewState.setIconText(station.title.substring(0, 3))
        viewState.setIconTextColor(textColor)
        viewState.setBackgroundColor(backgroundColor)
        rootPresenter.viewState.showControls(false)
    }

    var backgroundColor = Color.LTGRAY
        set(value) {
            field = value
            viewState.setBackgroundColor(value)
        }
    var textColor = Color.BLACK
        set(value) {
            field = value
            viewState.setIconTextColor(value)
        }

    fun setText(string: String) {
        viewState.setIconText(string)
    }

    fun option(url: Boolean, name: Boolean, add: Boolean) {
        viewState.option(url, name, add)
    }

    fun saveIcon(bitmap: Bitmap) {
        repository.cacheStationIcon(bitmap)
        exit()
    }

    fun onBackPressed(): Boolean {
        exit()
        return true
    }

    fun exit() {
        rootPresenter.viewState.showControls(true)
        router.exit()
    }

    fun optionId(checkedId: Int) {
        when (checkedId) {
            R.id.optionServerUrlBt -> {
                viewState.setIconImage(repository.getStationIcon(station.uri).blockingGet())
                viewState.option(true, false, false)
            }
            R.id.optionStationUrlBt -> {
                viewState.setIconImage(repository.getStationIcon(station.url).blockingGet())
                viewState.option(true, false, false)
            }
            R.id.optionNameBt -> {
                viewState.option(false, true, false)
            }
        }
    }
}