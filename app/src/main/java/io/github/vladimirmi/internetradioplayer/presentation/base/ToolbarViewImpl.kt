package io.github.vladimirmi.internetradioplayer.presentation.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_menu_item.view.*

/**
 * Created by Vladimir Mikhalev 11.11.2018.
 */

class ToolbarViewImpl : ToolbarView {

    lateinit var activity: AppCompatActivity
    private var menuHolder: MenuHolder? = null
    private var popupHelper: MenuPopupHelper? = null

    override fun setToolbarHost(activity: AppCompatActivity) {
        this.activity = activity
    }

    override fun setToolbarVisible(visible: Boolean) {
        if (visible) {
            activity.toolbar.visibility = View.VISIBLE
        } else {
            activity.toolbar.visibility = View.GONE
        }
    }

    override fun setToolbarTitle(@StringRes titleId: Int) {
        setToolbarTitle(activity.getString(titleId))
    }

    override fun setToolbarTitle(title: String) {
        activity.supportActionBar?.title = title
    }

    override fun enableBackNavigation(backNavEnabled: Boolean) {
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(backNavEnabled)
    }

    override fun setMenu(menuHolder: MenuHolder) {
        this.menuHolder = menuHolder
        activity.invalidateOptionsMenu()
    }

    @SuppressLint("RestrictedApi")
    override fun dismissToolbarMenu() {
        popupHelper?.dismiss()
    }

    override fun prepareOptionsMenu(menu: Menu) {
        menuHolder?.let { holder ->
            holder.menu.filter { it.showAsAction }
                    .forEachIndexed { index, item ->
                        menu.add(0, item.itemTitleResId, index, item.itemTitleResId).apply {
                            setIcon(item.iconResId)
                            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                            setOnMenuItemClickListener { holder.actions?.invoke(it); true }
                        }
                    }
            configurePopupFor(menu, holder)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun configurePopupFor(menu: Menu, holder: MenuHolder) {
        val popupItems = holder.menu.filter { !it.showAsAction }
        if (popupItems.isEmpty()) return

        val anchorItem = menu.add(R.string.menu_more).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        val anchorView = LayoutInflater.from(activity).inflate(R.layout.view_menu_item, activity.toolbar, false)
        anchorView.icon.setImageResource(R.drawable.ic_more)
        anchorItem.actionView = anchorView

        //todo expose popup instead od popupHelper
        val popup = PopupMenu(activity, anchorView)
        popupItems.forEachIndexed { index, item ->
            popup.menu.add(0, item.itemTitleResId, index, item.itemTitleResId).apply {
                setIcon(item.iconResId)
                icon.mutate().setTintExt(item.color)
            }
        }
        popup.setOnMenuItemClickListener {
            val consumed = activity.onOptionsItemSelected(it)
            if (!consumed) holder.actions?.invoke(it)
            true
        }

        popupHelper = MenuPopupHelper(activity, popup.menu as MenuBuilder, anchorView)
        popupHelper?.setForceShowIcon(true)

        anchorView.setOnClickListener {
            popupHelper?.show(0, -anchorView.height)
        }
    }
}
