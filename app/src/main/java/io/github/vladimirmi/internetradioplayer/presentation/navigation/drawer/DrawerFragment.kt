package io.github.vladimirmi.internetradioplayer.presentation.navigation.drawer

import android.animation.ValueAnimator
import android.os.Handler
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.lock
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.navigation.Navigator
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import kotlinx.android.synthetic.main.fragment_drawer.*
import kotlinx.android.synthetic.main.view_toolbar.view.*
import ru.terrakok.cicerone.NavigatorHolder
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 08.04.2019.
 */

class DrawerFragment : BaseFragment<DrawerPresenter, DrawerView>(), DrawerView {

    @Inject lateinit var navigatorHolder: NavigatorHolder
    private val navigator by lazy { Navigator(requireActivity(), R.id.rootContainer) }
    private lateinit var adapter: DrawerAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private var isHomeAsUp = false

    override val layout = R.layout.fragment_drawer

    override fun providePresenter(): DrawerPresenter {
        Toothpick.inject(this, Scopes.rootActivity)
        return Scopes.rootActivity.getInstance(DrawerPresenter::class.java)
    }

    override fun setupView(view: View) {
        setupMenu()
    }

    override fun onStart() {
        navigatorHolder.setNavigator(navigator)
        navigator.navigationIdListener = {
            adapter.selectItem(it)
            showDirectory(it == R.id.nav_search)
            setHomeAsUp(it == R.id.nav_settings || it == R.id.nav_equalizer)
            (requireActivity() as RootActivity).presenter
                    .checkPlayerVisibility(isPlayerEnabled = it != R.id.nav_settings)
        }
        super.onStart()
    }

    override fun onStop() {
        navigator.navigationIdListener = null
        navigatorHolder.removeNavigator()
        super.onStop()
    }

    fun init(drawerLayout: DrawerLayout, toolbar: Toolbar) {
        this.drawerLayout = drawerLayout
        this.toolbar = toolbar
        setupToggle()
    }

    private fun setupMenu() {
        val menu = PopupMenu(requireContext(), null).menu
        MenuInflater(requireContext()).inflate(R.menu.menu_drawer, menu)

        adapter = DrawerAdapter(menu)
        drawerRv.adapter = adapter
        drawerRv.layoutManager = LinearLayoutManager(requireContext())
        drawerRv.addItemDecoration(DrawerItemDecoration(requireContext()))

        adapter.onItemSelectedListener = { navigateTo(it) }
    }

    private fun setupToggle() {
        toggle = ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.desc_open_drawer,
                R.string.desc_close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.setToolbarNavigationClickListener { requireActivity().onBackPressed() }
    }

    private fun navigateTo(item: MenuItem) {
        drawerLayout.closeDrawers()
        presenter.navigateTo(item)
    }

    private fun showDirectory(show: Boolean) {
        toolbar.directoryLogoIv.visible(show)
    }

    private fun setHomeAsUp(homeAsUp: Boolean) {
        if (isHomeAsUp == homeAsUp) return
        isHomeAsUp = homeAsUp
        drawerLayout.lock(isHomeAsUp)
        val anim = if (isHomeAsUp) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
        anim.addUpdateListener { valueAnimator ->
            toggle.onDrawerSlide(drawerLayout, valueAnimator.animatedValue as Float)
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 400
        if (isHomeAsUp) {
            Handler().postDelayed({ toggle.isDrawerIndicatorEnabled = false }, 400)
        } else {
            toggle.isDrawerIndicatorEnabled = true
            toggle.onDrawerSlide(drawerLayout, 1f)
        }
        anim.start()
    }
}