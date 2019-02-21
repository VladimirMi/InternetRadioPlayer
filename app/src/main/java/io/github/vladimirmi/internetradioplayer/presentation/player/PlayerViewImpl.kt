package io.github.vladimirmi.internetradioplayer.presentation.player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.EMPTY_METADATA
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

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    private var startPlayX = 0f
    private var startPlayY = 0f
    private var startStatusY = 0f

    override fun setupView() {
        waitForLayout {
            startPlayX = playPauseBt.x
            startPlayY = playPauseBt.y
            startStatusY = statusTv.y
            true
        }
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
        //todo implement
    }

    override fun showStopped() {
        playPauseBt.isPlaying = false
//        bufferingPb.visible(false)
    }

    override fun showPlaying() {
        playPauseBt.isPlaying = true
//        bufferingPb.visible(false)
    }

    override fun showBuffering() {
        playPauseBt.isPlaying = true
//        bufferingPb.visible(true)
        setMetadata(EMPTY_METADATA)
    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setMetadata(metadata: MediaMetadataCompat) {
//        if (metadataCv == null) return
//        val visible = !metadata.isEmpty()
//        val scale = if (visible) 1f else 0f
//
//        metadataCv.animate()
//                .scaleX(scale).scaleY(scale)
//                .setDuration(300)
//                .setInterpolator(FastOutSlowInInterpolator())
//                .start()
//        if (visible) metadataCv.visible(true)
//        else Handler().postDelayed({ metadataCv?.visible(false) }, 300)
//        with(metadata) {
//            metaTitleTv.setTextOrHide(artist)
//            metaSubtitleTv.setTextOrHide(title)
//        }
    }

    //endregion

    private fun TextView.setTextOrHide(text: String?) {
        if (text == null || text.isBlank()) {
            visible(false)
        } else {
            visible(true)
            this.text = text
        }
    }

    fun setState(state: Float) {
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
}