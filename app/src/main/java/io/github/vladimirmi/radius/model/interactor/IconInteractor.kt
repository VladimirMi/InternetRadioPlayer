package io.github.vladimirmi.radius.model.interactor

import android.graphics.Bitmap
import android.graphics.Color
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.repository.StationRepository
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 20.12.2017.
 */

class IconInteractor
@Inject constructor(private val stationRepository: StationRepository) {

    private var initialBackGroundColor = Color.LTGRAY
    private var initialTextColor = Color.BLACK
    private var initialText = stationRepository.currentStation.value.title.substring(0, 3)

    var backGroundColor = initialBackGroundColor
    var textColor = initialTextColor
    var text = initialText
    var optionId = R.id.optionServerUrlBt

    val isIconChanged
        get() = backGroundColor != initialBackGroundColor
                || textColor != initialTextColor
                || text != initialText

    fun cacheIcon(bitmap: Bitmap) {
        stationRepository.cacheStationIcon(bitmap)
    }
}