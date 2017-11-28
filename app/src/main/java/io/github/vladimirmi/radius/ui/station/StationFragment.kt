package io.github.vladimirmi.radius.ui.station

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.station.StationPresenter
import io.github.vladimirmi.radius.presentation.station.StationView
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_station.*
import kotlinx.android.synthetic.main.part_station_info.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class StationFragment : BaseFragment(), StationView {
    override val layoutRes = R.layout.fragment_station

    companion object {
        fun newInstance(station: Station): StationFragment {
            return StationFragment().apply {
                arguments = Bundle().apply { putString("id", station.id) }
            }
        }
    }

    @InjectPresenter lateinit var presenter: StationPresenter

    @ProvidePresenter
    fun providePresenter(): StationPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(StationPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.id = arguments.getString("id")
    }

    override fun setStation(station: Station) {
        title.setTextWithoutAnimation(station.title)
        group.setTextWithoutAnimation(station.group)
        station.url?.let { url.setTextWithoutAnimation(it) } ?: url.remove()
        station.bitrate?.toString()?.let { bitrate.setTextWithoutAnimation(it + "kbps") }
                ?: bitrate.setTextWithoutAnimation("n/a")
        station.source?.toString()?.let { sample.setTextWithoutAnimation(it + "Hz") }
                ?: sample.setTextWithoutAnimation("n/a")
    }
}

private fun TextInputLayout.setTextWithoutAnimation(string: String?) {
    if (string == null) return
    isHintAnimationEnabled = false
    editText?.setText(string)
    isHintAnimationEnabled = true
}

fun View.remove() {
    visibility = View.GONE
}


