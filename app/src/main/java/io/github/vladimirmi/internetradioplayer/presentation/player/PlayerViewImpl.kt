package io.github.vladimirmi.internetradioplayer.presentation.player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.EMPTY_METADATA
import io.github.vladimirmi.internetradioplayer.data.service.artist
import io.github.vladimirmi.internetradioplayer.data.service.title
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseViewGroup
import kotlinx.android.synthetic.main.view_controls.view.*
import kotlinx.android.synthetic.main.view_player.view.*
import kotlinx.android.synthetic.main.view_station_info.view.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 20.02.2019.
 */

class PlayerViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseViewGroup<PlayerPresenter, PlayerView>(context, attrs, defStyleAttr), PlayerView {

    companion object {
        const val STATE_INIT = -1f
    }

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
        favoriteBt.setOnClickListener { presenter.switchFavorite() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }

        metaTitleTv.isSelected = true
        metaSubtitleTv.isSelected = true
        playPauseBt.setOnClickListener { presenter.playPause() }
        playPauseBt.setManualMode(true)
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        stopBt.setOnClickListener { presenter.stop() }
        equalizerBt.setOnClickListener { presenter.openEqualizer() }
        recordBt.setOnClickListener { presenter.scheduleRecord() }
    }

    //region =============== PlayerView ==============

    override fun setMedia(media: Media) {
        titleTv.text = media.name
        if (media is Station) {
            //todo move to presenter
            specsTv.setTextOrHide(media.specs)
        }
    }

    override fun setFavorite(isFavorite: Boolean) {
        val tint = if (isFavorite) R.color.orange_500 else R.color.primary_light
        favoriteBt.background.setTintExt(context!!.color(tint))
    }

    override fun setGroup(group: String) {
        groupTv.text = group
    }

    override fun showPaused() {
        playPauseBt.isPlaying = false
        statusTv.text = "Paused"
    }

    override fun showPlaying() {
        playPauseBt.isPlaying = true
        statusTv.text = "Playing"
    }

    override fun showBuffering() {
        playPauseBt.isPlaying = true
        statusTv.text = "Loading"
        setMetadata(EMPTY_METADATA)
    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setMetadata(metadata: MediaMetadataCompat) {
        with(metadata) {
            metaTitleTv.setTextOrHide(artist)
            metaSubtitleTv.setTextOrHide(title)
        }
    }

    override fun openLinkDialog(url: String) {
        val fm = (context as FragmentActivity).supportFragmentManager
        LinkDialog.newInstance(url).show(fm, "link_dialog")
    }

    override fun openAddShortcutDialog() {
        val fm = (context as FragmentActivity).supportFragmentManager
        AddShortcutDialog().show(fm, "add_shortcut_dialog")
    }

    //endregion

    private var startPlayX = 0f
    private var startPlayY = 0f
    private var startStatusY = 0f

    fun setState(state: Float) {
        if (state == STATE_INIT) {
            initStartState(); return
        }
        val set = ConstraintSet()
        set.clone(this)
        set.setVerticalBias(R.id.controlsView, state)
        set.applyTo(this)

        val visible = state > 0.9
        nextBt.visible(visible, false)
        previousBt.visible(visible, false)
        stopBt.visible(visible, false)
        progressSb.visible(visible, false)

        playPauseBt.x = startPlayX + (playPauseBtStub.x - startPlayX) * state
        playPauseBt.y = startPlayY + (playPauseBtStub.y - startPlayY) * state
        statusTv.y = startStatusY + (height - statusTv.height - startStatusY) * state
    }

    private fun initStartState() {
        waitForLayout {
            startPlayX = playPauseBt.x
            startPlayY = playPauseBt.y
            startStatusY = statusTv.y
            true
        }
        setState(0f)
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