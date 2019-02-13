package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import android.view.View
import com.google.android.material.tabs.TabLayout
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFrameView
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView
import io.github.vladimirmi.internetradioplayer.presentation.main.SimpleControlsView
import kotlinx.android.synthetic.main.fragment_favorite_list.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoriteListFragment : BaseFragment<FavoriteListPresenter, BaseView>(){

    override val layout = R.layout.fragment_favorite_list
    private var contentView: BaseView? = null

    override fun providePresenter(): FavoriteListPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(FavoriteListPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        navigationTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                contentView = when (tab.text) {
                    getString(R.string.stations) -> layoutInflater.inflate(R.layout.view_favorite_stations, null) as BaseView
                    getString(R.string.records) -> layoutInflater.inflate(R.layout.view_records, null) as BaseView
                    else -> throw IllegalStateException()
                }
                (container.getChildAt(0) as? BaseView)?.onDestroy()
                container.removeAllViews()
                container.addView(contentView as View)
                contentView?.onStart()
            }
        })
        navigationTl.selectTab(navigationTl.getTabAt(0))
    }

    override fun onStart() {
        super.onStart()
        contentView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        contentView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentView?.onDestroy()
    }
}
