package io.github.vladimirmi.radius.ui.root

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.di.module.RootActivityModule
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.MenuItemHolder
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.presentation.root.RootView
import io.github.vladimirmi.radius.presentation.root.ToolbarView
import io.github.vladimirmi.radius.ui.mediaList.MediaListFragment
import io.github.vladimirmi.radius.ui.station.StationFragment
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_menu_item.view.*
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : MvpAppCompatActivity(), RootView, ToolbarView {

    @Inject lateinit var navigatorHolder: NavigatorHolder
    @InjectPresenter lateinit var presenter: RootPresenter

    private val toolBarMenuItems = ArrayList<MenuItemHolder>()

    @ProvidePresenter
    fun providePresenter(): RootPresenter = Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
            Toothpick.inject(this@RootActivity, this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setSupportActionBar(toolbar)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onDestroy() {
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //todo Standalone class
    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {

        private var currentKey: String? = null

        override fun createActivityIntent(screenKey: String, data: Any?) = null

        override fun createFragment(screenKey: String, data: Any?): Fragment? {
            if (currentKey == screenKey) return null
            return when (screenKey) {
                Screens.MEDIA_LIST_SCREEN -> MediaListFragment()
                Screens.STATION_SCREEN -> StationFragment.newInstance(data as Station)
                else -> null
            }
        }

        override fun applyCommand(command: Command?) {
            currentKey = with(supportFragmentManager) {
                if (backStackEntryCount > 0) {
                    getBackStackEntryAt(backStackEntryCount - 1)?.name
                } else null
            }
            super.applyCommand(command)
        }

        override fun unknownScreen(command: Command?) {
            //do nothing
        }
    }

    //region =============== RootView ==============

    override fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    //endregion

    //region =============== ToolbarView ==============
    override fun setToolbarVisible(visible: Boolean) {
        if (visible) {
            toolbar.visibility = View.VISIBLE
        } else {
            toolbar.visibility = View.GONE
        }
    }

    override fun setToolbarTitle(@StringRes titleId: Int) {
        supportActionBar?.setTitle(titleId)
    }

    override fun enableBackNavigation(backNavEnabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(backNavEnabled)
    }

    override fun setMenuItems(menuItems: List<MenuItemHolder>) {
        toolBarMenuItems.clear()
        toolBarMenuItems.addAll(menuItems)
        invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (toolBarMenuItems.isNotEmpty()) {
            for (menuItemHolder in toolBarMenuItems) {
                val item = menu.add(menuItemHolder.itemTitle)
                if (menuItemHolder.hasPopupMenu()) {
                    configurePopupFor(item, menuItemHolder)
                } else {
                    item.setIcon(menuItemHolder.iconResId)
                    item.setOnMenuItemClickListener {
                        menuItemHolder.actions(it)
                        true
                    }
                }
                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("RestrictedApi")
    private fun configurePopupFor(item: MenuItem, menuItemHolder: MenuItemHolder) {
        val actionView = LayoutInflater.from(this).inflate(R.layout.view_menu_item, toolbar, false)
        actionView.icon.setImageResource(menuItemHolder.iconResId)
        item.actionView = actionView

        val popup = PopupMenu(this, actionView)
        popup.inflate(menuItemHolder.popupMenu!!)
        popup.setOnMenuItemClickListener {
            menuItemHolder.actions(it)
            true
        }
        val menuHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, actionView)
        (0 until popup.menu.size())
                .filter { popup.menu.getItem(it).icon != null }
                .forEach { menuHelper.setForceShowIcon(true); return@forEach }
        actionView.setOnClickListener {
            if (!menuHelper.isShowing) menuHelper.show(0, -actionView.height)
        }
    }

    //endregion
}