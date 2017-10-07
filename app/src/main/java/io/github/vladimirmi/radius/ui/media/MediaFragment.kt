package io.github.vladimirmi.radius.ui.media

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.presentation.media.MediaPresenter
import io.github.vladimirmi.radius.presentation.media.MediaView
import io.github.vladimirmi.radius.ui.base.BaseFragment
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaFragment : BaseFragment(), MediaView {

    override val layoutRes = R.layout.fragment_media
    private val adapter = MediaListAdapter()

    @InjectPresenter lateinit var presenter: MediaPresenter

    @ProvidePresenter
    fun providePresenter(): MediaPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MediaPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun setMediaList(mediaList: List<Media>) {
        adapter.setData(mediaList)
    }
}