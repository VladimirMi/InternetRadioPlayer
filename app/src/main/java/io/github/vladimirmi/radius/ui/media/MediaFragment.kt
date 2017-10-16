package io.github.vladimirmi.radius.ui.media

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.presentation.media.MediaPresenter
import io.github.vladimirmi.radius.presentation.media.MediaView
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_media.*
import timber.log.Timber
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaFragment : BaseFragment(), MediaView, MediaCallback {

    override val layoutRes = R.layout.fragment_media
    private val adapter = MediaListAdapter(this)

    @InjectPresenter lateinit var presenter: MediaPresenter

    @ProvidePresenter
    fun providePresenter(): MediaPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MediaPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        media_recycler.layoutManager = LinearLayoutManager(context)
        media_recycler.adapter = adapter
    }

    override fun setMediaList(mediaList: List<Media>) {
        Timber.e("setMediaList: ${mediaList.size}")
        adapter.setData(mediaList)
    }

    override fun onPlayPause(uri: Uri) {
        presenter.playPause(uri)
    }
}