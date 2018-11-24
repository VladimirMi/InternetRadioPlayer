package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.view.View
import android.widget.Toast
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.bounceXAnimation
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_player_controls.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class PlayerControlFragment : BaseFragment<PlayerControlPresenter, PlayerControlView>(),
        PlayerControlView {

    override val layout = R.layout.fragment_player_controls

    override fun providePresenter(): PlayerControlPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerControlPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        metadataTv.isSelected = true
        playPauseBt.setOnClickListener { presenter.playPause() }
        playPauseBt.setManualMode(true)
        iconIv.setOnClickListener { presenter.showStation() }
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        changeIconBt.setOnClickListener { presenter.changeIcon() }
        bufferingPb.indeterminateDrawable.mutate().setTintExt(context!!.color(R.color.pause_button))
    }

    override fun showStopped() {
        playPauseBt.isPlaying = false
        bufferingPb.visible(false)
    }

    override fun showPlaying() {
        playPauseBt.isPlaying = true
        bufferingPb.visible(false)
    }

    override fun showLoading() {
        playPauseBt.isPlaying = true
        bufferingPb.visible(true)
    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setStation(station: Station) {
//        iconIv.setImageBitmap(station.icon.getBitmap(context!!))
    }

    override fun setMetadata(metadata: String) {
        metadataTv.text = metadata
    }

    override fun enableEditMode(enable: Boolean) {
        changeIconBt.visible(enable)
    }

    override fun showMessage(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
}
