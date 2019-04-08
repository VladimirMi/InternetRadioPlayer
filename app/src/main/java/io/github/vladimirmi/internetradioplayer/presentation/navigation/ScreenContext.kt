package io.github.vladimirmi.internetradioplayer.presentation.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.presentation.data.DataFragment
import java.util.*

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class ScreenContext(val title: String, val parent: ScreenContext?) {

    val id = UUID.randomUUID().toString()

    var children = ArrayList<ScreenContext>()
        private set
    var endpoint: String? = null
        private set
    var query: String? = null
        private set
    var position: Int = 0
    private var fragment: Class<out Fragment>? = null

    fun screen(title: String, init: ScreenContext.() -> Unit = {}): ScreenContext {
        val child = ScreenContext(title, this)
        child.init()
        children.add(child)
        return child
    }

    fun stationsScreen(title: String, query: String = title, init: ScreenContext.() -> Unit = {}) {
        screen(title, init).data(UberStationsService.STATIONS_ENDPOINT, query)
    }

    fun topSongsScreen(query: String = title) {
        screen("Top songs").data(UberStationsService.TOPSONGS_ENDPOINT, query)
    }

    fun talksScreen(title: String, query: String = title) {
        screen(title).data(UberStationsService.TALKS_ENDPOINT, query)
    }

    fun <T : Class<out Fragment>> fragment(clazz: T) {
        fragment = clazz
    }

    fun findScreen(id: String): ScreenContext? {
        if (this.id == id) return this

        for (child in children) {
            val screen = child.findScreen(id)
            if (screen != null) return screen
        }
        return null
    }

    fun createFragment(): Fragment {
        return fragment?.newInstance() ?: DataFragment.newInstance(this)
    }

    fun createSmallView(inflater: LayoutInflater, root: ViewGroup): View {
        return inflater.inflate(R.layout.item_search_navigation, root, false).also {
            (it as Button).text = title
        }
    }

    private fun data(endpoint: String, query: String) {
        this.endpoint = endpoint
        this.query = query
    }

    override fun toString(): String {
        return "ScreenContext(title='$title', parent=${parent?.title}, children=${children.size})"
    }
}