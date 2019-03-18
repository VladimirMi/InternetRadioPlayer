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
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.utils.AudioEffects
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.root.RootView
import io.github.vladimirmi.internetradioplayer.utils.SimpleOnSeekBarChangeListener
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_controls.*
import kotlinx.android.synthetic.main.view_station_info.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 20.02.2019.
 */

const val PLAYER_STATE = "PLAYER_STATE"

class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView {


    override val layout = R.layout.fragment_player

    private lateinit var playerBehavior: BottomSheetBehavior<View>
    private var isSeekEnabled = false

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        titleTv.isSelected = true
        metaTitleTv.isSelected = true
        metaSubtitleTv.isSelected = true
        simpleMetaTv.isSelected = true
        playPauseBt.setManualMode(true)

        setupButtons()
        setupSeekBar()
        setupBehavior()
    }

    private fun setupButtons() {
        favoriteBt.setOnClickListener { presenter.switchFavorite() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        stopBt.setOnClickListener { presenter.stop() }
        recordBt.setOnClickListener { presenter.startStopRecording() }
        pointerIv.setOnClickListener { switchState() }
        equalizerBt.setOnClickListener {
            if (playerBehavior.isExpanded) switchState()
            presenter.openEqualizer()
        }
        playPauseBt.setOnClickListener {
            presenter.playPause()
            if (isSeekEnabled) presenter.seekTo(progressSb.progress)
        }
    }

    private fun setupSeekBar() {
        progressSb.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) presenter.seekTo(progress)
                positionTv.text = Formats.duration(progress.toLong())
            }
        })
    }

    private fun setupBehavior() {
        requireView().post {
            playerBehavior = BottomSheetBehavior.from(view)
            playerBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    setOffset(slideOffset)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }
            })
            setupOffset()
        }
    }

    override fun handleBackPressed(): Boolean {
        return if (playerBehavior.isExpanded) {
            switchState()
            true
        } else false
    }

    //region =============== PlayerView ==============

    override fun setStation(station: Station) {
        titleTv.text = station.name
        specsTv.text = station.specs
        addShortcutBt.visible(true)
        equalizerBt.visible(AudioEffects.isEqualizerSupported())
        favoriteBt.visible(true)
        recordBt.visible(true)
    }

    override fun setRecord(record: Record) {
        titleTv.text = record.name
        specsTv.text = record.createdAtString
        setGroup("")
        addShortcutBt.visible(false)
        equalizerBt.visible(false)
        favoriteBt.visible(false)
        recordBt.visible(false)
    }

    override fun setFavorite(isFavorite: Boolean) {
        //todo refactor (create field inside station)
        val tint = if (isFavorite) R.color.orange_500 else R.color.primary_variant
        favoriteBt.background.setTintExt(context!!.color(tint))
    }

    override fun setGroup(group: String) {
        groupTv.setTextOrHide(Group.getViewName(group, requireContext()))
    }

    override fun setStatus(resId: Int) {
        statusTv.setText(resId)
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

    override fun incrementPositionBy(duration: Long) {
        progressSb.incrementProgressBy(duration.toInt())
    }

    override fun setDuration(duration: Long) {
        durationTv.text = Formats.duration(duration)
        progressSb.max = duration.toInt()
    }

    override fun enableSeek(isEnabled: Boolean) {
        isSeekEnabled = isEnabled
        progressSb.visible(isEnabled)
        positionTv.visible(isEnabled)
        durationTv.visible(isEnabled)
    }

    override fun enableSkip(isEnabled: Boolean) {
        val tint = requireContext().color(if (isEnabled) R.color.grey_50 else R.color.grey_600)
        nextBt.setColorFilter(tint)
        previousBt.setColorFilter(tint)
        nextBt.isEnabled = isEnabled
        previousBt.isEnabled = isEnabled
    }

    //endregion

    private fun setupOffset() {
        when {
            playerBehavior.isCollapsed -> setOffset(0f)
            playerBehavior.isExpanded -> setOffset(1f)
            playerBehavior.isHidden -> setOffset(-1f)
        }
    }

    private fun setOffset(offset: Float) {
        val parentState = Util.constrainValue(offset + 1, 0f, 1f)
        val state = Util.constrainValue(offset, 0f, 1f)
        (activity as? RootView)?.setOffset(parentState)

        val playerView = view as? ConstraintLayout ?: return
        val set = ConstraintSet()
        set.clone(playerView)
        set.setVerticalBias(R.id.controlsView, state)
        set.applyTo(playerView)

        val visible = state > 0.9
        nextBt.visible(visible, false)
        previousBt.visible(visible, false)
        stopBt.visible(visible, false)
        if (isSeekEnabled) {
            progressSb.visible(visible, false)
            positionTv.visible(visible, false)
            durationTv.visible(visible, false)
        }
        simpleMetaFl.visible(state == 0f)

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

    private fun switchState() {
        if (playerBehavior.isCollapsed) {
            playerBehavior.isExpanded = true
        } else if (playerBehavior.isExpanded) {
            playerBehavior.isCollapsed = true
        }
    }
}

var BottomSheetBehavior<View>.isExpanded
    get() = state == BottomSheetBehavior.STATE_EXPANDED
    set(value) {
        state = if (value) BottomSheetBehavior.STATE_EXPANDED
        else BottomSheetBehavior.STATE_COLLAPSED
    }
var BottomSheetBehavior<View>.isCollapsed
    get() = state == BottomSheetBehavior.STATE_COLLAPSED
    set(value) {
        if (value) state = BottomSheetBehavior.STATE_COLLAPSED
    }
var BottomSheetBehavior<View>.isHidden
    get() = state == BottomSheetBehavior.STATE_HIDDEN
    set(value) {
        state = if (value) BottomSheetBehavior.STATE_HIDDEN
        else BottomSheetBehavior.STATE_COLLAPSED
    }