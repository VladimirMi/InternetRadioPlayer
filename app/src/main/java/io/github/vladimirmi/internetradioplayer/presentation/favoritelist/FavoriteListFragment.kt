package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.View
import com.google.android.material.tabs.TabLayout
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records.RecordsView
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations.EditDialog
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations.FavoriteStationsView
import kotlinx.android.synthetic.main.fragment_favorite_list.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoriteListFragment : BaseFragment<FavoriteListPresenter, FavoriteListView>(),
        FavoriteListView, EditDialog.Callback {

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

            @SuppressLint("InflateParams")
            override fun onTabSelected(tab: TabLayout.Tab) {
                contentView = when (tab.position) {
                    0 -> layoutInflater.inflate(R.layout.view_favorite_stations, container, false) as BaseView
                    1 -> layoutInflater.inflate(R.layout.view_records, container, false) as BaseView
                    else -> throw IllegalStateException()
                }
                (container.getChildAt(0) as? BaseView)?.apply {
                    onStop()
                    onDestroy()
                }
                container.removeAllViews()
                container.addView(contentView as View)
                contentView?.onStart()
            }
        })
        navigationTl.addTab(navigationTl.newTab().apply {
            text = getString(R.string.stations)
        })
        navigationTl.addTab(navigationTl.newTab().apply {
            text = getString(R.string.records)
        })
        navigationTl.getTabAt(0)?.select()
    }

    override fun showTabs(visible: Boolean) {
        navigationTl.visible(visible)
        if (!visible) navigationTl.getTabAt(0)?.select()
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

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedItem = getContextSelectedItem()
        when (selectedItem) {
            is Station -> when {
                item.itemId == R.id.context_menu_action_edit -> openStationEditDialog(selectedItem)
                item.itemId == R.id.context_menu_action_delete -> presenter.deleteStation(selectedItem)
                else -> return false

            }
            is Record -> if (item.itemId == R.id.context_menu_action_delete) {
                presenter.deleteRecord(selectedItem)
            } else {
                return false
            }
            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    override fun onDialogEdit(newText: String) {
        val station = getContextSelectedItem() as? Station ?: return
        presenter.editStation(station, newText)
    }

    private fun openStationEditDialog(station: Station) {
        //todo to strings
        EditDialog.newInstance("Edit station", "Station name", station.name)
                .show(childFragmentManager, "edit_station_dialog")
    }

    private fun getContextSelectedItem(): Any {
        return (contentView as? FavoriteStationsView)?.getContextSelectedItem()
                ?: (contentView as? RecordsView)?.getContextSelectedItem()
                ?: Any()
    }
}
