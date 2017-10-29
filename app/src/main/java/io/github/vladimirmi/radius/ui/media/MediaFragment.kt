package io.github.vladimirmi.radius.ui.media

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
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
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaFragment : BaseFragment(), MediaView, MediaItemCallback {

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

    override fun setMediaList(mediaList: LiveData<List<Media>>) {
        mediaList.observe(this, Observer { adapter.setData(it!!) })
    }

    override fun onItemSelected(media: Media) {
        presenter.select(media)
    }

    override fun select(media: Media, playing: Boolean) {
        findMediaGroupItemVH(media).select(playing)
    }

    override fun unselect(media: Media) {
        findMediaGroupItemVH(media).unselect()
    }

    private fun findMediaGroupItemVH(media: Media): MediaGroupItemVH {
        val position = adapter.getItemPosition(media)
        return media_recycler.findViewHolderForLayoutPosition(position) as MediaGroupItemVH
    }
}