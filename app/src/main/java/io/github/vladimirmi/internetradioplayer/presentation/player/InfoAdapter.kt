package io.github.vladimirmi.internetradioplayer.presentation.player

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager.widget.PagerAdapter
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseCustomView
import io.github.vladimirmi.internetradioplayer.presentation.player.coverart.CoverArtViewImpl
import io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo.MediaInfoViewImpl

/**
 * Created by Vladimir Mikhalev 26.03.2019.
 */

class InfoAdapter(private val lifecycle: Lifecycle) : PagerAdapter() {

    var coverArtEnabled = true
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = if (position == 0) {
            MediaInfoViewImpl(container.context)
        } else {
            CoverArtViewImpl(container.context)
        }
        container.addView(view)
        lifecycle.addObserver(view as LifecycleObserver)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        lifecycle.removeObserver(obj as LifecycleObserver)
        (obj as BaseCustomView<*, *>).onStop()
        container.removeView(obj)
        obj.onDestroy()
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return if (coverArtEnabled) 2 else 1
    }

    override fun getItemPosition(obj: Any): Int {
        return if (!coverArtEnabled && obj is CoverArtViewImpl) POSITION_NONE
        else POSITION_UNCHANGED
    }
}