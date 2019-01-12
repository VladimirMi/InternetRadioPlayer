package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import android.view.View
import android.widget.SeekBar
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.waitForMeasure
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.utils.SimpleOnSeekBarChangeListener
import kotlinx.android.synthetic.main.fragment_equalizer.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerFragment : BaseFragment<EqualizerPresenter, EqualizerView>(), EqualizerView {

    override val layout = R.layout.fragment_equalizer

    override fun providePresenter(): EqualizerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(EqualizerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
    }

    override fun setBands(bands: List<String>, values: List<Int>, min: Int, max: Int) {
        view?.waitForMeasure {
            equalizerView.setBands(bands, values, min, max)
        }

        equalizerView.onBandLevelChangeListener = presenter::setBandLevel

        bassSb.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) presenter.setBassBoost(progress)
            }
        })
        virtualSb.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) presenter.setVirtualizer(progress)
            }
        })
    }

    override fun setBassBoost(bassBoost: Int) {
        bassSb.progress = bassBoost
    }

    override fun setVirtualizer(virtualizer: Int) {
        virtualSb.progress = virtualizer
    }
}