package io.github.vladimirmi.internetradioplayer.presentation.main

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.di.Scopes
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
                arguments = Bundle().apply { putInt(Preferences.KEY_MAIN_PAGE_ID, page) }
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
        val pageId = arguments?.getInt(Preferences.KEY_MAIN_PAGE_ID) ?: 0
        setPageId(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }
        })
    }

    //region =============== MainView ==============

    override fun setPageId(pageId: Int) {
        arguments = Bundle().apply { putInt(Preferences.KEY_MAIN_PAGE_ID, pageId) }
        val page = when (pageId) {
            R.id.nav_search -> PAGE_SEARCH
            R.id.nav_favorites -> PAGE_FAVORITES
            else -> PAGE_HISTORY
        }
        mainPager.setCurrentItem(page, false)
    }

    override fun showPlayerView(visible: Boolean) {
        val bottomPadding: Int = if (visible) {
            requireContext().resources.getDimension(R.dimen.player_collapsed_height).toInt()
        } else {
            0
        }
        mainContent.setPadding(mainContent.paddingLeft, mainContent.paddingTop, mainContent.paddingRight,
                bottomPadding)
    }

    //endregion
}
