package io.github.vladimirmi.radius.presentation.root

import android.support.annotation.StringRes
import android.view.MenuItem
import io.github.vladimirmi.radius.R
import java.util.*

class ToolbarBuilder {
    private var isToolbarVisible = true
    @StringRes private var toolbarTitleId = R.string.app_name
    private var backNavEnabled = false
    private val menuItems = ArrayList<MenuItemHolder>()

    fun setToolbarVisible(toolbarVisible: Boolean): ToolbarBuilder {
        isToolbarVisible = toolbarVisible
        return this
    }

    fun setToolbarTitleId(@StringRes titleId: Int): ToolbarBuilder {
        toolbarTitleId = titleId
        return this
    }

    fun setBackNavigationEnabled(backEnabled: Boolean): ToolbarBuilder {
        backNavEnabled = backEnabled
        return this
    }

    fun addAction(menuItemHolder: MenuItemHolder): ToolbarBuilder {
        menuItems.add(menuItemHolder)
        return this
    }

    fun build(toolbarView: ToolbarView) {
        toolbarView.setToolbarVisible(isToolbarVisible)
        toolbarView.setToolbarTitle(toolbarTitleId)
        toolbarView.enableBackNavigation(backNavEnabled)
        toolbarView.setMenuItems(menuItems)
    }
}

class MenuItemHolder(val itemTitle: String,
                     val iconResId: Int,
                     val actions: (MenuItem) -> Unit,
                     val popupMenu: Int? = null) {
    //todo action to weak ref
    fun hasPopupMenu() = popupMenu != null
}