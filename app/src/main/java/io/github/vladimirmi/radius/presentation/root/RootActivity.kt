package io.github.vladimirmi.radius.presentation.root

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
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
import io.github.vladimirmi.radius.extensions.visible
import io.github.vladimirmi.radius.model.service.PlayerService
import io.github.vladimirmi.radius.navigation.Navigator
import io.github.vladimirmi.radius.ui.base.BackPressListener
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_menu_item.view.*
import ru.terrakok.cicerone.NavigatorHolder
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : MvpAppCompatActivity(), RootView, ToolbarView {

    @Inject lateinit var navigatorHolder: NavigatorHolder
    @InjectPresenter lateinit var presenter: RootPresenter

    private val navigator = Navigator(this, R.id.mainFr)
    private var menuHolder: MenuHolder? = null
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
        if (intent?.hasExtra(PlayerService.EXTRA_STATION_ID) == true) {
            presenter.showStation(intent.getStringExtra(PlayerService.EXTRA_STATION_ID))
        }
        intent?.data?.let { presenter.addStation(it) }
        intent = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    @SuppressLint("RestrictedApi")
    override fun onDestroy() {
//        popupHelper?.dismiss()
//        popupHelper = null
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    override fun onBackPressed() {
        val handled = supportFragmentManager?.fragments?.any {
            (it as? BackPressListener)?.onBackPressed() ?: false
        } ?: false
        if (!handled) super.onBackPressed()
    }

    //region =============== RootView ==============

    override fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showSnackbar(resId: Int) {
        Snackbar.make(findViewById(android.R.id.content), resId, Snackbar.LENGTH_LONG).show()
    }

    override fun showControls(visible: Boolean) {
        playerControlsFr.view?.visible(visible)
    }

    override fun showMetadata(visible: Boolean) {
        metadataFr.view?.visible(visible)
    }

    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
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

    override fun setMenu(menuHolder: MenuHolder) {
        this.menuHolder = menuHolder
        invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuHolder?.let { holder ->
            holder.menu.filter { it.showAsAction }
                    .forEachIndexed { index, item ->
                        menu.add(0, item.itemTitleResId, index, item.itemTitleResId).apply {
                            setIcon(item.iconResId)
                            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                            setOnMenuItemClickListener {
                                holder.actions.invoke(it)
                                true
                            }
                        }
                    }
            configurePopupFor(menu, holder)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("RestrictedApi")
    private fun configurePopupFor(menu: Menu, holder: MenuHolder) {
        val popupItems = holder.menu.filter { !it.showAsAction }
        if (popupItems.isEmpty()) return

        val anchorItem = menu.add(R.string.menu_more).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        val anchorView = LayoutInflater.from(this).inflate(R.layout.view_menu_item, toolbar, false)
        anchorView.icon.setImageResource(R.drawable.ic_more)
        anchorItem.actionView = anchorView

        val popup = PopupMenu(this, anchorView)
        popupItems.forEachIndexed { index, item ->
            popup.menu.add(0, item.itemTitleResId, index, item.itemTitleResId).apply {
                setIcon(item.iconResId)
            }
        }
        popup.setOnMenuItemClickListener {
            holder.actions.invoke(it)
            true
        }

        popupHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, anchorView)
        (0 until popup.menu.size())
                .map { popup.menu.getItem(it) }
                .forEach {
                    val drawable = DrawableCompat.wrap(it.icon).mutate()
                    DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.black))
                    popupHelper?.setForceShowIcon(true)
                }

        anchorView.setOnClickListener {
            popupHelper?.show(0, -anchorView.height)
        }
    }
    //endregion
}