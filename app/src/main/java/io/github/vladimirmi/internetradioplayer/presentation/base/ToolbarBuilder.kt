package io.github.vladimirmi.internetradioplayer.presentation.base

import android.graphics.Color
import android.support.annotation.StringRes
import android.view.MenuItem
import io.github.vladimirmi.internetradioplayer.R

class ToolbarBuilder {
    private var isToolbarVisible = true
    @StringRes private var titleId = R.string.app_name
    private var title = ""
    private var backNavEnabled = false
    private val menuHolder = MenuHolder()

    fun setToolbarVisible(toolbarVisible: Boolean = true): ToolbarBuilder {
        isToolbarVisible = toolbarVisible
        return this
    }

    fun setToolbarTitleId(@StringRes titleId: Int): ToolbarBuilder {
        this.titleId = titleId
        return this
    }

    fun setToolbarTitle(title: String): ToolbarBuilder {
        this.title = title
        return this
    }

    fun enableBackNavigation(enable: Boolean = true): ToolbarBuilder {
        backNavEnabled = enable
        return this
    }

    fun setMenuActions(actions: (MenuItem) -> Unit): ToolbarBuilder {
        menuHolder.actions = actions
        return this
    }

    fun addMenuItem(menuItem: MenuItemHolder): ToolbarBuilder {
        menuHolder.addItem(menuItem)
        return this
    }

    fun removeMenuItem(id: Int): ToolbarBuilder {
        menuHolder.removeItem(id)
        return this
    }

    fun removeMenuItem(menuItem: MenuItemHolder): ToolbarBuilder {
        menuHolder.removeItem(menuItem.id)
        return this
    }

    fun clearMenu(): ToolbarBuilder {
        menuHolder.clear()
        return this
    }

    fun build(toolbarView: ToolbarView) {
        toolbarView.setToolbarVisible(isToolbarVisible)
        if (title.isNotBlank()) {
            toolbarView.setToolbarTitle(title)
        } else {
            toolbarView.setToolbarTitle(titleId)
        }
        toolbarView.enableBackNavigation(backNavEnabled)
        toolbarView.setMenu(menuHolder.sorted())
    }
}

class MenuHolder {
    private val items = ArrayList<MenuItemHolder>()
    lateinit var actions: (MenuItem) -> Unit
    val menu: List<MenuItemHolder> get() = items

    fun addItem(item: MenuItemHolder) {
        if (!items.contains(item)) items.add(item)
    }

    fun removeItem(id: Int) {
        items.removeAll { it.id == id }
    }

    fun clear() {
        items.clear()
    }

    fun sorted(): MenuHolder {
        return this.apply { items.sortBy(MenuItemHolder::order) }
    }
}

class MenuItemHolder(val itemTitleResId: Int,
                     val iconResId: Int,
                     val showAsAction: Boolean = false,
                     val order: Int = 0,
                     val color: Int = Color.DKGRAY) {

    val id = itemTitleResId
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuItemHolder

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}
