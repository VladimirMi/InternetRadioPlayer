package io.github.vladimirmi.internetradioplayer.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.Visibility
import androidx.viewpager.widget.ViewPager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.MAIN_PAGE_ID_KEY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainFragment : BaseFragment<MainPresenter, MainView>(), MainView {

    override val layout = R.layout.fragment_main

    companion object {
        fun newInstance(page: Int): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply { putInt(MAIN_PAGE_ID_KEY, page) }
            }
        }
    }

    override fun providePresenter(): MainPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MainPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        mainPager.adapter = MainPagerAdapter(context!!, childFragmentManager)
        mainTl.setupWithViewPager(mainPager)
        val pageId = arguments?.getInt(MAIN_PAGE_ID_KEY) ?: 0
        setPageId(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }
        })
        setupToolbar()
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        val item = menu.add(0, R.string.menu_add_station, 0, R.string.menu_add_station)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        item.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_add)
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_add_station -> openAddStationDialog()
            else -> return false
        }
        return true
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val drawer = findDrawer(toolbar) ?: return
        val toggle = ActionBarDrawerToggle(activity, drawer, toolbar, R.string.desc_expand_collapse,
                R.string.desc_favorite)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun findDrawer(view: View?): DrawerLayout? {
        if (view == null) return null
        if (view is DrawerLayout) return view
        return findDrawer(view.parent as? View)
    }

    override fun setPageId(pageId: Int) {
        arguments = Bundle().apply { putInt(MAIN_PAGE_ID_KEY, pageId) }
        val page = when (pageId) {
            R.id.nav_search -> 0
            R.id.nav_stations -> 1
            else -> 2
        }
        mainPager.setCurrentItem(page, true)
    }

    override fun showControls(visible: Boolean) {
        val slide = createSlideTransition()
        slide.mode = if (visible) Visibility.MODE_IN else Visibility.MODE_OUT
        TransitionManager.beginDelayedTransition(view as ViewGroup, slide)
        playerControlsFr.view?.visible(visible)
    }

    private fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }

    private fun createSlideTransition(): Slide {
        val slide = Slide()
        slide.slideEdge = Gravity.BOTTOM
        slide.duration = 300
        slide.addTarget(R.id.playerControlsFr)
        slide.interpolator = FastOutSlowInInterpolator()
        return slide
    }
}
