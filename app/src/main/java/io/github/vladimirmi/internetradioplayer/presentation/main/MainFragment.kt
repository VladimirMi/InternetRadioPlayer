package io.github.vladimirmi.internetradioplayer.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.MAIN_PAGE_ID_KEY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.waitForMeasure
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.view_controls_simple.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainFragment : BaseFragment<MainPresenter, MainView>(), MainView {

    override val layout = R.layout.fragment_main
    private var prevPage = PAGE_SEARCH
    private var controlsVisible = false

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
        mainPager.offscreenPageLimit = 3
        mainTl.setupWithViewPager(mainPager)
        val pageId = arguments?.getInt(MAIN_PAGE_ID_KEY) ?: 0
        setPageId(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val visibility = Math.min(1f, Math.abs(PAGE_PLAYER - (position + positionOffset)))
                showControls(visibility)
            }
        })
        setupToolbar()

        sPlayPauseBt.setManualMode(true)
        sPlayPauseBt.setOnClickListener { presenter.playPause() }
    }

    private fun showControls(visibility: Float) {
        simpleControlsContainer.waitForMeasure {
            val set = ConstraintSet()
            set.clone(mainCl)
            set.setMargin(R.id.mainPager, ConstraintSet.BOTTOM,
                    (simpleControlsContainer.height * visibility).toInt())
            set.applyTo(mainCl)
            controlsVisible = visibility > 0
        }
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

    //region =============== MainView ==============

    override fun setPageId(pageId: Int) {
        arguments = Bundle().apply { putInt(MAIN_PAGE_ID_KEY, pageId) }
        val page = when (pageId) {
            R.id.nav_search -> PAGE_SEARCH
            R.id.nav_favorites -> PAGE_FAVORITES
            R.id.nav_player -> PAGE_PLAYER
            else -> PAGE_HISTORY
        }
        prevPage = page
        mainPager.setCurrentItem(page, true)
    }


    override fun showStopped() {
        sPlayPauseBt.setPlaying(false, controlsVisible)
//        bufferingPb.visible(false)
    }

    override fun showPlaying() {
        sPlayPauseBt.setPlaying(true, controlsVisible)
//        bufferingPb.visible(false)
    }

    override fun showBuffering() {
        sPlayPauseBt.setPlaying(true, controlsVisible)
//        bufferingPb.visible(true)
    }

    override fun setMetadata(metadata: String) {
        sMetadataTv.text = metadata
    }

    //endregion

    private fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }
}
