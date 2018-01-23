package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

@InjectViewState
class IconPickerPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val stationInteractor: StationInteractor,
                    private val mediaController: MediaController,
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

    var iconRes: IconRes = IconRes.ICON_1
        set(value) {
            field = value
            viewState.setIconRes(value)
            viewState.setForegroundColor(foregroundColor)
        }

    override fun onFirstViewAttach() {
        stationInteractor.currentIcon.let {
            viewState.buildToolbar(ToolbarBuilder().setToolbarTitle(it.name))
            when (iconOption) {
                IconOption.ICON -> iconRes = it.iconRes
                IconOption.FAVICON -> viewState.setIconImage(it.bitmap)
            }
            backgroundColor = it.backgroundColor
            foregroundColor = it.foregroundColor
            text = it.text
            iconOption = it.option
        }

        rootPresenter.viewState.showControls(false)
        rootPresenter.viewState.showMetadata(false)
    }

    fun saveIcon(bitmap: Bitmap) {
        val icon = Icon(
                name = station.name,
                bitmap = bitmap,
                option = iconOption,
                iconRes = iconRes,
                backgroundColor = backgroundColor,
                foregroundColor = foregroundColor,
                text = text
        )
        stationInteractor.currentIcon = icon
        exit()
    }

    fun onBackPressed(): Boolean {
        exit()
        return true
    }

    fun exit() {
        rootPresenter.viewState.showControls(true)
        if (!mediaController.isStopped) {
            rootPresenter.viewState.showMetadata(true)
        }
        router.exit()
    }

}