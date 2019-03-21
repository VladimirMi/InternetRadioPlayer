package io.github.vladimirmi.internetradioplayer.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class NavigationScreen(val title: String, val parent: NavigationScreen?) {

    var children = ArrayList<NavigationScreen>()
        private set
    private var fragment: Class<out Fragment>? = null

    fun screen(title: String, init: NavigationScreen.() -> Unit = {}) {
        val child = NavigationScreen(title, this)
        child.init()
        children.add(child)
    }

    fun <T : Class<out Fragment>> fragment(clazz: T) {
        fragment = clazz
    }

    fun findScreen(title: String): NavigationScreen? {
        if (this.title == title) return this

        for (child in children) {
            val screen = child.findScreen(title)
            if (screen != null) return screen
        }
        return null
    }

    fun createFragment(): Fragment {
        return fragment?.newInstance() ?: NavigationFragment.newInstance(this)
    }

    fun createSmallView(inflater: LayoutInflater, root: ViewGroup): View {
        return inflater.inflate(R.layout.item_navigation, root, false).also {
            (it as Button).text = title
        }
    }

    override fun toString(): String {
        return "NavigationScreen(title='$title', parent=${parent?.title}, children=${children.size})"
    }
}