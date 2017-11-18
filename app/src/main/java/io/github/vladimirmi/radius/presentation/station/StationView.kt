package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.Station

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : MvpView {
    fun setStation(station: Station)
}