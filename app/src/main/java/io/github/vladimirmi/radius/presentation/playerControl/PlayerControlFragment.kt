package io.github.vladimirmi.radius.presentation.playerControl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.getIconTextColors
import io.github.vladimirmi.radius.extensions.waitForMeasure
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
        media_icon.setOnClickListener { presenter.showStation() }
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
        val colors = context.getIconTextColors(station.title[0])
        icon_text.text = station.title[0].toString().toUpperCase()
        icon_text.setTextColor(colors.first)
        icon_text.waitForMeasure {
            presenter.saveBitmap(loadBitmapFromView(icon_text))
        }
    }

    private fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        v.draw(Canvas(b))
        return b
    }
}