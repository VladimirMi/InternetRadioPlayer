package io.github.vladimirmi.radius.presentation.playerControl

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.setTint
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.source.StationIconSource
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

    override fun setMedia(station: Station) {
        favorite.setBackgroundResource(if (station.favorite) R.drawable.ic_star else R.drawable.ic_empty_star)

        val iconSource = StationIconSource(context)
        val colors = iconSource.getIconTextColors(station)

        iconIv.setImageBitmap(iconSource.getBitmap(station,
                colors.copy(second = ContextCompat.getColor(context, R.color.transparent))))
        presenter.saveBitmap(iconSource.getBitmap(station))
    }

    override fun createMode(createMode: Boolean) {
        previous.setTint(if (createMode) R.color.grey_400 else R.color.grey_700)
        next.setTint(if (createMode) R.color.grey_400 else R.color.grey_700)
        previous.isEnabled = !createMode
        next.isEnabled = !createMode
    }
}