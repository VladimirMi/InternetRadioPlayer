package io.github.vladimirmi.radius.ui.playercontrol

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.presentation.playercontrol.PlayerControlPresenter
import io.github.vladimirmi.radius.presentation.playercontrol.PlayerControlView
import io.github.vladimirmi.radius.ui.base.BaseFragment
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
}