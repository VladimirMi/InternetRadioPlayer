package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.extensions.ioToMain
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.interactor.IconInteractor
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

@InjectViewState
class IconPickerPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val iconInteractor: IconInteractor,
                    stationInteractor: StationInteractor,
                    private val router: Router)
    : BasePresenter<IconPickerView>() {

    private val station = stationInteractor.currentStation

    var backgroundColor: Int = 0
        set(value) {
            field = value
            viewState.setBackgroundColor(value)
        }
    var textColor: Int = 0
        set(value) {
            field = value
            viewState.setIconTextColor(value)
        }

    var text: String = ""
        set(value) {
            field = value
            viewState.setIconText(value)
        }

    var checkedOption: IconOption = IconOption.SERVER
        set(value) {
            field = value
            viewState.setOption(value)
            Observable.create<String> { e ->
                if (!e.isDisposed) {
                    val path = when (value) {
                        IconOption.DEFAULT -> station.title
                        IconOption.SERVER -> station.uri
                        IconOption.STATION -> station.url
                        IconOption.TEXT -> text
                    }
                    e.onNext(path)
                }
            }.switchMapSingle { iconInteractor.getIcon(it) }
                    .ioToMain()
                    .subscribe { viewState.setIconImage(it.bitmap) }
                    .addTo(compDisp)
        }

    override fun onFirstViewAttach() {
        if (station.url.isBlank()) viewState.hideStationUrlOption()
        if (station.title.isBlank()) viewState.hideTextOption()

        iconInteractor.getCurrentIcon()
                .ioToMain()
                .subscribeBy {
                    viewState.setIconImage(it.bitmap)
                    backgroundColor = it.backGroundColor
                    textColor = it.textColor
                    text = it.text
                    checkedOption = it.option
                }
                .addTo(compDisp)

        rootPresenter.viewState.showControls(false)
    }

    fun saveIcon(bitmap: Bitmap) {
        val icon = Icon(
                name = station.title,
                bitmap = bitmap,
                backGroundColor = backgroundColor,
                textColor = textColor,
                text = text,
                option = checkedOption
        )
        iconInteractor.cacheIcon(icon)
                .subscribe { exit() }
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