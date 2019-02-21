package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerConfig
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerPreset
import io.github.vladimirmi.internetradioplayer.extensions.setProgressX
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.extensions.waitForMeasure
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.ui.EqualizerContainer
import io.github.vladimirmi.internetradioplayer.utils.SimpleOnSeekBarChangeListener
import kotlinx.android.synthetic.main.view_controls_simple.*
import kotlinx.android.synthetic.main.view_equalizer.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerFragment : BaseFragment<EqualizerPresenter, EqualizerView>(), EqualizerView {

    private lateinit var presetAdapter: ArrayAdapter<String>
    private var change = false

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
                presenter.selectPreset(position)
            }
        }

        switchBindBt.setOnClickListener { presenter.switchBind() }
        resetBt.setOnClickListener { presenter.resetCurrentPreset() }

        sPlayPauseBt.setManualMode(true)
        sPlayPauseBt.setOnClickListener { presenter.playPause() }
        sMetadataTv.isSelected = true
//        sBufferingPb.indeterminateDrawable.setTintExt(requireContext().color(R.color.pause_button))
    }

    override fun setupEqualizer(config: EqualizerConfig) {
        view?.waitForMeasure {
            equalizerView.setBands(config.bands, config.minLevel, config.maxLevel)
        }
        equalizerView.onBandLevelChangeListener = object : EqualizerContainer.OnBandLevelChangeListener {
            override fun onBandLevelChange(band: Int, level: Int) {
                presenter.setBandLevel(band, level)
                change = true
            }

            override fun onStopChange(band: Int) {
                presenter.saveCurrentPreset()
                change = false
            }
        }
        bassSb.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    presenter.setBassBoost(progress)
                    change = true
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                presenter.saveCurrentPreset()
                change = false
            }
        })
        virtualSb.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    presenter.setVirtualizer(progress)
                    change = true
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                presenter.saveCurrentPreset()
                change = false
            }
        })
    }

    override fun setPreset(preset: EqualizerPreset) {
        with(preset) {
            presetSpinner.setSelection(presetAdapter.getPosition(name))
            equalizerView.setBandLevels(bandLevels, animate = !change)
            bassSb.setProgressX(bassBoostStrength, animate = !change)
            virtualSb.setProgressX(virtualizerStrength, animate = !change)
        }
    }

    override fun setPresetNames(presets: List<String>) {
        presetAdapter.clear()
        presetAdapter.addAll(presets)
        presetAdapter.notifyDataSetChanged()
    }

    override fun setBindIcon(iconResId: Int) {
        switchBindBt.setImageResource(iconResId)
    }

    override fun showReset(show: Boolean) {
        resetBt.visible(show)
    }

    override fun showStopped() {
        sPlayPauseBt.isPlaying = false
//        sBufferingPb.visible(false)
    }

    override fun showPlaying() {
        sPlayPauseBt.isPlaying = true
//        sBufferingPb.visible(false)
    }

    override fun showBuffering() {
        sPlayPauseBt.isPlaying = true
//        sBufferingPb.visible(true)
    }

    override fun setMetadata(metadata: String) {
        sMetadataTv.text = metadata
    }
}