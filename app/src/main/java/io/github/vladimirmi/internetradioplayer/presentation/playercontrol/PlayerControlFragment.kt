package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
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
        playPauseBt.setOnClickListener { presenter.playPause() }
        iconIv.setOnClickListener { presenter.showStation() }
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        changeIconBt.setOnClickListener { presenter.changeIcon() }
    }

    override fun showStopped() {
        playPauseBt.setBackgroundResource(R.drawable.ic_play)
    }

    override fun showPlaying() {
        playPauseBt.setBackgroundResource(R.drawable.ic_stop)
    }

    override fun setStation(station: Station) {
        iconIv.setImageBitmap(station.icon.getBitmap(context!!))
    }

    override fun enableEditMode(enable: Boolean) {
        changeIconBt.visible(enable)
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
}
