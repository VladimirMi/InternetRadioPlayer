package io.github.vladimirmi.internetradioplayer.presentation.player

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_controls.*
import kotlinx.android.synthetic.main.view_station_info.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 20.02.2019.
 */

class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView {

    companion object {
        const val STATE_INIT = -1f
    }

    override val layout = R.layout.fragment_player

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        favoriteBt.setOnClickListener { presenter.switchFavorite() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }

        metaTitleTv.isSelected = true
        metaSubtitleTv.isSelected = true
        simpleMetaTv.isSelected = true
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
        val tint = if (isFavorite) R.color.orange_500 else R.color.primary_variant
        favoriteBt.background.setTintExt(context!!.color(tint))
    }

    override fun setGroup(group: String) {
        groupTv.text = group
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

    override fun setMetadata(artist: String, title: String) {
        metaTitleTv.setTextOrHide(artist)
        metaSubtitleTv.setTextOrHide(title)
    }

    override fun setSimpleMetadata(metadata: String) {
        simpleMetaTv.text = metadata
    }

    //endregion

    private var startPlayX = 0f
    private var startPlayY = 0f
    private var startStatusY = 0f

    fun setState(state: Float) {
        if (state == STATE_INIT) {
            initStartState(); return
        }
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

        playPauseBt.x = startPlayX + (playPauseBtStub.x - startPlayX) * state
        playPauseBt.y = startPlayY + (playPauseBtStub.y - startPlayY) * state
        statusTv.y = startStatusY + (playerView.height - statusTv.height - startStatusY) * state
    }

    private fun initStartState() {
        view?.waitForLayout {
            startPlayX = playPauseBt.x
            startPlayY = playPauseBt.y
            startStatusY = statusTv.y
            true
        }
        setState(0f)
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