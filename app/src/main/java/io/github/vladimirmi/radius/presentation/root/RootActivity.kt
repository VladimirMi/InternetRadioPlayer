package io.github.vladimirmi.radius.presentation.root

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.di.module.RootActivityModule
import io.github.vladimirmi.radius.navigation.Navigator
import io.github.vladimirmi.radius.ui.base.BackPressListener
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_menu_item.view.*
import ru.terrakok.cicerone.NavigatorHolder
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : MvpAppCompatActivity(), RootView, ToolbarView {

    @Inject lateinit var navigatorHolder: NavigatorHolder
    @InjectPresenter lateinit var presenter: RootPresenter

    private val navigator = Navigator(this, R.id.fragment_container)
    private val toolBarMenuItems = ArrayList<MenuItemHolder>()
    private var popupHelper: MenuPopupHelper? = null

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
        popupHelper?.dismiss()
        popupHelper = null
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        val handled = supportFragmentManager?.fragments?.any {
            (it as? BackPressListener)?.onBackPressed() ?: false
        } ?: false
        if (!handled) presenter.onBackPressed()
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
        setToolbarTitle(getString(titleId))
    }

    override fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
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
            menuItemHolder.actions.invoke(it)
            true
        }

        popupHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, actionView)
        (0 until popup.menu.size())
                .map { popup.menu.getItem(it) }
                .forEach {
                    val drawable = DrawableCompat.wrap(it.icon).mutate()
                    DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.black))
                    popupHelper?.setForceShowIcon(true)
                }

        actionView.setOnClickListener {
            Timber.e("configurePopupFor: click")
            popupHelper?.show(0, -actionView.height)
        }
    }
    //endregion
}