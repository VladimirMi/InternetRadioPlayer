package io.github.vladimirmi.internetradioplayer.presentation.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 26.03.2019.
 */

class InfoAdapter : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = if (position == 0) R.layout.view_cover_art
        else R.layout.view_media_info

        val view = LayoutInflater.from(container.context).inflate(layout, container, false)
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