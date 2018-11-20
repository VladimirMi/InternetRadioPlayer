package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.data.utils.EXTRA_PLAY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.RootActivityModule
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.Navigator
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseActivity
import kotlinx.android.synthetic.main.activity_root.*
import ru.terrakok.cicerone.NavigatorHolder
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : BaseActivity<RootPresenter, RootView>(), RootView {

    @Inject lateinit var navigatorHolder: NavigatorHolder

    override val layout = R.layout.activity_root
    private val navigator by lazy { Navigator(this, R.id.mainFr) }

    override fun providePresenter(): RootPresenter = Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
            Toothpick.inject(this@RootActivity, this)
        }
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null || (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            intent = null // stop redeliver old intent
        }
    }

    override fun setupView() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            when {
                menuItem.groupId == R.id.menu_group_main -> presenter.openMainScreen(menuItem.itemId)
                menuItem.itemId == R.id.menu_item_exit -> presenter.exitApp()
                menuItem.itemId == R.id.menu_item_settings -> presenter.openSettings()
            }
            true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        Timber.e("onNewIntent: ")
        super.onNewIntent(intent)
        this.intent = intent
        if (isPresenterInitialized) checkIntent()
    }

    override fun onDestroy() {
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_settings -> presenter.openSettings()
            R.string.menu_exit -> presenter.exitApp()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //region =============== RootView ==============

    override fun checkIntent() {
        if (intent == null) return
        val startPlay = intent.getBooleanExtra(EXTRA_PLAY, false)
        if (intent.hasExtra(PlayerService.EXTRA_STATION_ID)) {
            //todo legacy
            presenter.showStation(intent.getStringExtra(PlayerService.EXTRA_STATION_ID), startPlay)
        } else {
            intent.data?.let { addStation(it, startPlay) }
        }
        intent = null
    }

    override fun showMessage(resId: Int) {
        Snackbar.make(activityView, resId, Snackbar.LENGTH_LONG).show()
    }


    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
    }

    override fun setCheckedDrawerItem(itemId: Int) {
        navigationView.setCheckedItem(itemId)
    }

    fun addStation(uri: Uri, startPlay: Boolean = false) {
        presenter.addStation(uri, startPlay)
    }

    //endregion
}
