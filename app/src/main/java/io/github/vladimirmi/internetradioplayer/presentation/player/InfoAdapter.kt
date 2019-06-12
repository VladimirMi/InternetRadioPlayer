package io.github.vladimirmi.internetradioplayer.presentation.player

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager.widget.PagerAdapter
import io.github.vladimirmi.internetradioplayer.presentation.player.coverart.CoverArtViewImpl
import io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo.MediaInfoViewImpl

/**
 * Created by Vladimir Mikhalev 26.03.2019.
 */

class InfoAdapter(private val lifecycle: Lifecycle) : PagerAdapter() {

    var coverArtEnabled = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = if (position == 0) {
            MediaInfoViewImpl(container.context)
        } else {
            CoverArtViewImpl(container.context)
        }
        (view as? LifecycleObserver)?.let { lifecycle.addObserver(it) }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return if (coverArtEnabled) 2 else 1
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }
}