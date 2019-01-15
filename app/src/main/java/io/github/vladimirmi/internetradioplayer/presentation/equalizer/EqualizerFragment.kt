package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    private lateinit var presetAdapter: ArrayAdapter<String>

    override val layout = R.layout.fragment_equalizer

    override fun providePresenter(): EqualizerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(EqualizerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        presetAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item)
        presetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        presetSpinner.adapter = presetAdapter

        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.selectPreset(presetAdapter.getItem(position) ?: "")
            }
        }
    }

    override fun setupBands(bands: List<String>, min: Int, max: Int) {
        view?.waitForMeasure {
            equalizerView.setBands(bands, min, max)
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

    override fun setBandLevels(bandLevels: List<Int>) {
        equalizerView.setBandLevels(bandLevels)
    }

    override fun setBassBoost(bassBoost: Int) {
        bassSb.progress = bassBoost
    }

    override fun setVirtualizer(virtualizer: Int) {
        virtualSb.progress = virtualizer
    }

    override fun setPresets(presets: List<String>, curPreset: Int) {
        presetAdapter.clear()
        presetAdapter.addAll(presets)
        presetAdapter.notifyDataSetChanged()
        presetSpinner.setSelection(curPreset)
    }
}