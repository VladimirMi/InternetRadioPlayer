package io.github.vladimirmi.internetradioplayer.presentation.player

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.AudioEffects
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.main.MainFragment
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_controls.*
import kotlinx.android.synthetic.main.view_station_info.*
import toothpick.Toothpick
import java.util.*

/**
 * Created by Vladimir Mikhalev 20.02.2019.
 */

class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView {

    override val layout = R.layout.fragment_player

    private lateinit var sheetBehavior: BottomSheetBehavior<View>

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        metaTitleTv.isSelected = true
        metaSubtitleTv.isSelected = true
        simpleMetaTv.isSelected = true
        equalizerBt.visible(AudioEffects.isEqualizerSupported())
        playPauseBt.setManualMode(true)

        favoriteBt.setOnClickListener { presenter.switchFavorite() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }
        playPauseBt.setOnClickListener { presenter.playPause(progressSb.progress) }
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        stopBt.setOnClickListener { presenter.stop() }
        equalizerBt.setOnClickListener { presenter.openEqualizer() }
        recordBt.setOnClickListener { presenter.scheduleRecord() }
        pointerIv.setOnClickListener { switchState() }

        setupBehavior(view)
    }

    private fun setupBehavior(view: View) {
        view.waitForLayout {
            sheetBehavior = BottomSheetBehavior.from(view)
            sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    setOffset(slideOffset)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }
            })
            setupOffset()
            true
        }
    }

    //region =============== PlayerView ==============

    override fun showPlayerView(visible: Boolean) {
        if (requireView().isVisible == visible) return
        requireView().visible(visible)
        (parentFragment as MainFragment?)?.showPlayerView(visible)
        if (visible) requireView().waitForLayout {
            setupOffset(); true
        }
    }

    override fun setMedia(media: Media) {
        titleTv.text = media.name
    }

    override fun setFavorite(isFavorite: Boolean) {
        val tint = if (isFavorite) R.color.orange_500 else R.color.primary_variant
        favoriteBt.background.setTintExt(context!!.color(tint))
    }

    override fun setGroup(group: String?) {
        groupTv.setTextOrHide(group)
    }

    override fun setStatus(resId: Int) {
        statusTv.setText(resId)
    }

    override fun setSpecs(specs: String) {
        specsTv.text = specs
    }

    override fun showPlaying(isPlaying: Boolean) {
        playPauseBt.isPlaying = isPlaying
    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setRecording(isRecording: Boolean) {
        val tint = requireContext().color(if (isRecording) R.color.secondary else R.color.primary_variant)
        recordBt.setColorFilter(tint)
    }

    override fun setMetadata(artist: String, title: String) {
        metaTitleTv.setTextOrHide(artist)
        metaSubtitleTv.setTextOrHide(title)
    }

    override fun setSimpleMetadata(metadata: String) {
        simpleMetaTv.text = metadata
    }

    override fun setPosition(position: Long) {
        progressSb.progress = position.toInt()
    }

    override fun increasePosition(duration: Long) {
        progressSb.progress = (progressSb.progress + duration).toInt()
    }

    override fun setDuration(duration: Long) {
        durationTv.text = Util.getStringForTime(StringBuilder(), Formatter(), duration)
        progressSb.max = duration.toInt()
        progressSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) presenter.seekTo(progress)
                //todo do not create sb and formatter
                positionTv.text = Util.getStringForTime(StringBuilder(), Formatter(), progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    //endregion

    private fun switchState() {
        if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupOffset() {
        if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) setOffset(0f)
        else if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) setOffset(1f)
    }

    private fun setOffset(state: Float) {
        val playerView = view as? ConstraintLayout ?: return
        val set = ConstraintSet()
        set.clone(playerView)
        set.setVerticalBias(R.id.controlsView, state)
        set.applyTo(playerView)

        val visible = state > 0.9
        nextBt.visible(visible, false)
        previousBt.visible(visible, false)
        stopBt.visible(visible, false)
        progressSb.visible(visible, false)

        simpleMetaTv.visible(state == 0f)

        playPauseBt.x = playPauseBtStart.x + (playPauseBtEnd.x - playPauseBtStart.x) * state
        playPauseBt.y = playPauseBtStart.y + (playPauseBtEnd.y - playPauseBtStart.y) * state
        statusTv.y = statusTvStart.y + (playerView.height - statusTv.height - statusTvStart.y) * state

        pointerIv.rotationX = 180 * state
    }

    private fun openLinkDialog(url: String) {
        LinkDialog.newInstance(url).show(childFragmentManager, "link_dialog")
    }

    private fun openAddShortcutDialog() {
        AddShortcutDialog().show(childFragmentManager, "add_shortcut_dialog")
    }

    private fun TextView.setTextOrHide(text: String?) {
        if (text == null || text.isBlank()) {
            visible(false)
        } else {
            visible(true)
            this.text = text
        }
    }

    private fun TextView.linkStyle(enable: Boolean) {
        val string = text.toString()
        val color = ContextCompat.getColor(context, R.color.blue_500)
        text = if (enable) {
            val spannable = SpannableString(string)
            spannable.setSpan(URLSpan(string), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(color), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable
        } else {
            string
        }
    }
}