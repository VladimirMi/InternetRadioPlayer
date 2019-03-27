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

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = if (position == 0) {
            CoverArtViewImpl(container.context)
        } else {
            MediaInfoViewImpl(container.context)
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
        return 2
    }
}