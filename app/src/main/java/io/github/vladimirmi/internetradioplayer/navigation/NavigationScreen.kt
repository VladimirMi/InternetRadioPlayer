package io.github.vladimirmi.internetradioplayer.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.presentation.navigation.NavigationFragment

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class NavigationScreen(val title: String, val parent: NavigationScreen?) {

    var children = ArrayList<NavigationScreen>()
        private set
    var endpoint: String? = null
        private set
    var query: String? = null
        private set
    private var fragment: Class<out Fragment>? = null

    fun screen(title: String, init: NavigationScreen.() -> Unit = {}): NavigationScreen {
        val child = NavigationScreen(title, this)
        child.init()
        children.add(child)
        return child
    }

    fun stationsScreen(title: String, query: String = title, init: NavigationScreen.() -> Unit = {}) {
        screen(title, init).stationsData(query)
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

    private fun stationsData(query: String) {
        this.endpoint = UberStationsService.STATIONS_ENDPOINT
        this.query = query
    }

    override fun toString(): String {
        return "NavigationScreen(title='$title', parent=${parent?.title}, children=${children.size})"
    }
}