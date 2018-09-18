package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.transition.Visibility
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.RootActivityModule
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.Navigator
import io.github.vladimirmi.internetradioplayer.ui.base.BackPressListener
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

    private val navigator by lazy { Navigator(this, R.id.mainFr) }
    private var menuHolder: MenuHolder? = null
    private var popupHelper: MenuPopupHelper? = null

    @ProvidePresenter
    fun providePresenter(): RootPresenter = Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
            Toothpick.inject(this@RootActivity, this)
        }
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_root)
        setSupportActionBar(toolbar)

        if (savedInstanceState != null || (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            intent = null // stop redeliver old intent
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        navigatorHolder.removeNavigator()
        popupHelper?.dismiss()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onDestroy() {
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

    override fun checkIntent() {
        if (intent != null) {
            if (intent.hasExtra(PlayerService.EXTRA_STATION_ID)) {
                presenter.showStation(intent.getStringExtra(PlayerService.EXTRA_STATION_ID))
            }
            intent.data?.let { addStation(it) }
            intent = null
        }
    }

    override fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showSnackbar(resId: Int) {
        Snackbar.make(findViewById(android.R.id.content), resId, Snackbar.LENGTH_LONG).show()
    }

    override fun showControls(visible: Boolean) {
        Timber.e("showControls: $visible")
        val slide = createSlideTransition()
        slide.mode = if (visible) Visibility.MODE_IN else Visibility.MODE_OUT
        TransitionManager.beginDelayedTransition(root, slide)
        playerControlsFr.view?.visible(visible)
    }

    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
    }

    fun addStation(uri: Uri) {
        presenter.addStation(uri)
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
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        val anchorView = LayoutInflater.from(this).inflate(R.layout.view_menu_item, toolbar, false)
        anchorView.icon.setImageResource(R.drawable.ic_more)
        anchorItem.actionView = anchorView

        //todo expose popup instead od popupHelper
        val popup = PopupMenu(this, anchorView)
        popupItems.forEachIndexed { index, item ->
            popup.menu.add(0, item.itemTitleResId, index, item.itemTitleResId).apply {
                setIcon(item.iconResId)
                icon.mutate().setTintExt(item.color)
            }
        }
        popup.setOnMenuItemClickListener {
            holder.actions.invoke(it)
            true
        }

        popupHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, anchorView)
        popupHelper?.setForceShowIcon(true)

        anchorView.setOnClickListener {
            popupHelper?.show(0, -anchorView.height)
        }
    }
    //endregion

    private fun createSlideTransition(): Slide {
        val slide = Slide()
        slide.slideEdge = Gravity.BOTTOM
        slide.duration = 300
        slide.addTarget(R.id.playerControlsFr)
        slide.interpolator = FastOutSlowInInterpolator()
        return slide
    }
}
