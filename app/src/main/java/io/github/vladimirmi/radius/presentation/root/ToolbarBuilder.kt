package io.github.vladimirmi.radius.presentation.root

import android.support.annotation.StringRes
import android.view.MenuItem
import io.github.vladimirmi.radius.R
import java.util.*

class ToolbarBuilder {
    private var isToolbarVisible = true
    @StringRes private var titleId = R.string.app_name
    private var title = ""
    private var backNavEnabled = false
    private val menuItems = ArrayList<MenuItemHolder>()

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

    fun addAction(menuItemHolder: MenuItemHolder): ToolbarBuilder {
        menuItems.add(menuItemHolder)
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
        toolbarView.setMenuItems(menuItems)
    }
}

class MenuItemHolder(val itemTitle: String,
                     val iconResId: Int,
                     val actions: (MenuItem) -> Unit,
                     val popupMenu: Int? = null) {

    // todo leak canary
    //todo action to weak ref
    fun hasPopupMenu() = popupMenu != null
}