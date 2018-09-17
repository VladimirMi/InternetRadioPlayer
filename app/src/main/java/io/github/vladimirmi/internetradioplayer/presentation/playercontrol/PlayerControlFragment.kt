package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_player_controls.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class PlayerControlFragment : BaseFragment(), PlayerControlView {

    override val layoutRes = R.layout.fragment_player_controls

    @InjectPresenter lateinit var presenter: PlayerControlPresenter

    @ProvidePresenter
    fun providePresenter(): PlayerControlPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerControlPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        metadataTv.isSelected = true
        playPauseBt.setOnClickListener { presenter.playPause() }
        playPauseBt.setManualMode(true)
        iconIv.setOnClickListener { presenter.showStation() }
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        changeIconBt.setOnClickListener { presenter.changeIcon() }
        loadingPb.indeterminateDrawable.mutate().setTintExt(context!!.color(R.color.red_800))
    }

    override fun showStopped() {
        playPauseBt.isPlaying = false
        loadingPb.visible(false)
    }

    override fun showPlaying() {
        playPauseBt.isPlaying = true
        loadingPb.visible(false)
    }

    override fun showLoading() {
        playPauseBt.isPlaying = true
        loadingPb.visible(true)
    }

    override fun setStation(station: Station) {
        iconIv.setImageBitmap(station.icon.getBitmap(context!!))
    }

    override fun setMetadata(metadata: String) {
        metadataTv.text = metadata
    }

    override fun enableEditMode(enable: Boolean) {
        changeIconBt.visible(enable)
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
}
