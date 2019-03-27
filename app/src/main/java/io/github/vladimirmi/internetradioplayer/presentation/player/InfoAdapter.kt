package io.github.vladimirmi.internetradioplayer.presentation.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager.widget.PagerAdapter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo.MediaInfoViewImpl

/**
 * Created by Vladimir Mikhalev 26.03.2019.
 */

class InfoAdapter(private val lifecycle: Lifecycle) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = if (position == 0) R.layout.view_cover_art
        else R.layout.view_media_info

        val view = if (position == 0) LayoutInflater.from(container.context).inflate(layout, container, false)
        else MediaInfoViewImpl(container.context)
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

    override fun finishUpdate(container: ViewGroup) {
//        onAdd.invoke()

    }
}