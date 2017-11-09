package io.github.vladimirmi.radius.ui.root

import android.os.Bundle
import android.support.v4.app.Fragment
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.di.module.RootActivityModule
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.presentation.root.RootView
import io.github.vladimirmi.radius.ui.media.MediaFragment
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportAppNavigator
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : MvpAppCompatActivity(), RootView {

    @Inject lateinit var navigatorHolder: NavigatorHolder
    @InjectPresenter lateinit var presenter: RootPresenter

    @ProvidePresenter
    fun providePresenter() : RootPresenter {
        return Toothpick.openScopes(Scopes.APP, Scopes.ROOT_ACTIVITY)
                .getInstance(RootPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Toothpick.openScopes(Scopes.APP, Scopes.ROOT_ACTIVITY).apply {
            installModules(RootActivityModule())
            Toothpick.inject(this@RootActivity, this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
        intent?.data?.let { presenter.addMedia(it) }
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

    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {

        override fun createActivityIntent(screenKey: String?, data: Any?) = null

        override fun createFragment(screenKey: String?, data: Any?): Fragment? = when (screenKey) {
            Screens.MEDIA_SCREEN -> MediaFragment()
            else -> null
        }
    }
}