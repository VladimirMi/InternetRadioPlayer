package io.github.vladimirmi.radius.presentation.playercontrol

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.color
import io.github.vladimirmi.radius.extensions.setTint
import io.github.vladimirmi.radius.extensions.setTintExt
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.ui.base.BaseFragment
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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        play_pause.setOnClickListener { presenter.playPause() }
        favorite.setOnClickListener { presenter.switchFavorite() }
        iconIv.setOnClickListener { presenter.showStation() }
        previous.setOnClickListener { presenter.skipPrevious() }
        next.setOnClickListener { presenter.skipNext() }
    }

    override fun showStopped() {
        play_pause.setBackgroundResource(R.drawable.ic_play)
    }

    override fun showPlaying() {
        play_pause.setBackgroundResource(R.drawable.ic_stop)
    }

    override fun setStation(station: Station) {
        if (station.favorite) {
            favorite.setBackgroundResource(R.drawable.ic_star)
        } else {
            favorite.background = ContextCompat.getDrawable(context, R.drawable.ic_empty_star).apply {
                mutate().setTintExt(ContextCompat.getColor(context, R.color.grey_700))
            }
        }
    }

    override fun setStationIcon(stationIcon: Bitmap) {
        iconIv.setImageBitmap(stationIcon)
    }

    override fun enableNextPrevious(enable: Boolean) {
        val tint = context.color(if (enable) R.color.grey_700 else R.color.grey_400)
        previous.setTint(tint)
        next.setTint(tint)
        previous.isEnabled = enable
        next.isEnabled = enable
    }
}