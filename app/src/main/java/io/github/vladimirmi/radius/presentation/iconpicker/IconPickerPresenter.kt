package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.interactor.IconInteractor
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
                    private val iconInteractor: IconInteractor,
                    private val router: Router)
    : BasePresenter<IconPickerView>() {

    private val station = repository.currentStation.value

    var backgroundColor: Int
        get() = iconInteractor.backGroundColor
        set(value) {
            iconInteractor.backGroundColor = value
            viewState.setBackgroundColor(value)
        }
    var textColor: Int
        get() = iconInteractor.textColor
        set(value) {
            iconInteractor.textColor = value
            viewState.setIconTextColor(value)
        }

    var text: String
        get() = iconInteractor.text
        set(value) {
            iconInteractor.text = value
            viewState.setIconText(value)
        }

    var checkedOptionId: Int
        get() = iconInteractor.optionId
        set(value) {
            iconInteractor.optionId = value
            viewState.setOptionId(value)
        }

    override fun onFirstViewAttach() {
        if (station.url.isBlank()) viewState.hideStationUrlOption()
        if (station.title.isBlank()) viewState.hideTextOption()

        viewState.setIconImage(repository.getStationIcon(station.uri).blockingGet())
        viewState.setOptionId(checkedOptionId)
        viewState.setIconText(text)
        viewState.setIconTextColor(textColor)
        viewState.setBackgroundColor(backgroundColor)
        rootPresenter.viewState.showControls(false)
    }

    fun saveIcon(bitmap: Bitmap) {
        iconInteractor.cacheIcon(bitmap)
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
}