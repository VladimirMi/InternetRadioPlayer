package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.model.entity.icon.IconOption
import io.github.vladimirmi.internetradioplayer.model.entity.icon.IconRes
import io.github.vladimirmi.internetradioplayer.model.entity.icon.IconResource
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.root.RootPresenter
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

@InjectViewState
class IconPickerPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val stationInteractor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenter<IconPickerView>() {

    private val station = stationInteractor.currentStation

    var backgroundColor: Int = 0
        set(value) {
            field = value
            viewState.setBackgroundColor(value)
        }

    var foregroundColor: Int = 0
        set(value) {
            field = value
            viewState.setForegroundColor(value)
        }

    var text: String = ""
        set(value) {
            field = value
            viewState.setIconText(value)
        }

    var iconOption: IconOption = IconOption.ICON
        set(value) {
            field = value
            viewState.setOption(value)
        }

    var iconResource: IconResource = IconResource.ICON_1
        set(value) {
            field = value
            viewState.setIconResource(value)
            viewState.setForegroundColor(foregroundColor)
        }

    override fun onFirstViewAttach() {
        stationInteractor.currentIcon.let {
            viewState.buildToolbar(ToolbarBuilder().setToolbarTitle(it.name))
            when (it) {
                is IconRes -> {
                    iconResource = it.res
                    foregroundColor = it.foregroundColor
                }
            }
            iconOption = it.option
        }
    }

    override fun attachView(view: IconPickerView?) {
        super.attachView(view)
        rootPresenter.viewState.showControls(false)
        rootPresenter.viewState.showMetadata(false)
    }

    fun saveIcon(bitmap: Bitmap) {
        @Suppress("WhenWithOnlyElse")
        val icon = when (iconOption) {
            else -> IconRes(station.name, foregroundColor, iconResource, bitmap)
        }
        stationInteractor.currentIcon = icon
        exit()
    }

    fun onBackPressed(): Boolean {
        exit()
        return true
    }

    fun exit() {
        rootPresenter.viewState.showControls(true)
        if (!controlsInteractor.isStopped) {
            rootPresenter.viewState.showMetadata(true)
        }
        router.exit()
    }

}