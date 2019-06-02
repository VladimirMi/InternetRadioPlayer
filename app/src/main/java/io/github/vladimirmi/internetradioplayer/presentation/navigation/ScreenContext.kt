package io.github.vladimirmi.internetradioplayer.presentation.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.data.DataFragment
import java.util.*

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

open class ScreenContext(val title: String) {

    constructor(@StringRes titleId: Int) : this(Scopes.context.getString(titleId))

    val id = UUID.randomUUID().toString()
    val children = ArrayList<ScreenContext>()
    var parent: ScreenContext? = null

    var position: Int = 0

    fun screen(@StringRes titleId: Int, init: ScreenContext.() -> Unit = {}) {
        ScreenContext(titleId).initScreenContext(this, init)
    }

    fun screen(title: String, init: ScreenContext.() -> Unit = {}) {
        ScreenContext(title).initScreenContext(this, init)
    }

    fun fragmentScreen(@StringRes titleId: Int, fragment: Class<out Fragment>, init: ScreenContext.() -> Unit = {}) {
        FragmentScreen(titleId, fragment).initScreenContext(this, init)
    }

    fun stationsScreen(title: String, query: String = title, init: DataScreen.() -> Unit = {}) {
        DataScreen(title, UberStationsService.STATIONS_ENDPOINT, query).initScreenContext(this, init)
    }

    fun topSongsScreen(query: String = title) {
        DataScreen("Top songs", UberStationsService.TOPSONGS_ENDPOINT, query).initScreenContext<ScreenContext>(this)
    }

    protected fun <T : ScreenContext> initScreenContext(parent: ScreenContext, init: T.() -> Unit = {}) {
        this.parent = parent
        @Suppress("UNCHECKED_CAST")
        (this as T).init()
        parent.children.add(this)
    }


    fun findScreen(predicate: (ScreenContext) -> Boolean): ScreenContext? {
        if (predicate(this)) return this

        for (child in children) {
            val screen = child.findScreen(predicate)
            if (screen != null) return screen
        }
        return null
    }

    fun createSmallView(inflater: LayoutInflater, root: ViewGroup): View {
        return inflater.inflate(R.layout.item_search_navigation, root, false).also {
            (it as Button).text = title
        }
    }

    open fun createFragment(): Fragment? = null

    override fun toString(): String {
        return "ScreenContext(title='$title', parent=${parent?.title}, children=${children.size})"
    }
}

class DataScreen(title: String, val endpoint: String, val query: String) : ScreenContext(title) {

    override fun createFragment(): Fragment {
        return DataFragment.newInstance(this)
    }
}

class FragmentScreen(@StringRes titleId: Int, private val fragment: Class<out Fragment>) : ScreenContext(titleId) {

    override fun createFragment(): Fragment? {
        return fragment.newInstance()
    }
}