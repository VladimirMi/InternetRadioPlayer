package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.main.NewStationDialog
import kotlinx.android.synthetic.main.fragment_favorite_stations.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class FavoriteStationsFragment : BaseFragment<FavoriteStationsPresenter, FavoriteStationsView>(),
        FavoriteStationsView, StationItemCallback {

    override val layout = R.layout.fragment_favorite_stations

    private val stationListAdapter by lazy { StationListAdapter(this) }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemSwipeCallback() {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                stationListAdapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onStartDrag(position: Int) {
                stationListAdapter.onStartDrag(position)
            }

            override fun onIdle() {
                presenter.moveGroupElements(stationListAdapter.onIdle())
            }
        })
    }

    override fun providePresenter(): FavoriteStationsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(FavoriteStationsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        stationsRv.layoutManager = LinearLayoutManager(context!!)
        stationsRv.adapter = stationListAdapter
        addStationFab.setOnClickListener { openAddStationDialog() }
//        itemTouchHelper.attachToRecyclerView(stationsRv)
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.add(0, R.string.menu_change_order, 0, R.string.menu_change_order).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            icon = ContextCompat.getDrawable(context!!, R.drawable.ic_sort)
        }
        menu.add(0, R.string.menu_add_group, 0, R.string.menu_add_group).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            icon = ContextCompat.getDrawable(context!!, R.drawable.ic_add)
        }
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_change_order -> {
            }
            R.string.menu_add_group -> {
            }
            else -> return false
        }
        return true
    }

    //region =============== StationListView ==============

    override fun setStations(stationList: FlatStationsList) {
        stationListAdapter.setData(stationList)
    }

    override fun selectStation(station: Station) {
        stationListAdapter.selectStation(station)
        val position = stationListAdapter.getPosition(station)
        if (position != -1) stationsRv.scrollToPosition(position)
    }

    override fun showPlaceholder(show: Boolean) {
        stationsRv.visible(!show)
        placeholderView.visible(show)
    }

    override fun getContextSelectedItem() = stationListAdapter.longClickedItem

    //endregion

    //region =============== StationItemCallback ==============

    override fun onGroupSelected(id: String) {
        presenter.selectGroup(id)
    }

    override fun onItemSelected(station: Station) {
        presenter.selectStation(station)
    }

    override fun onGroupRemove(id: String) {
        presenter.removeGroup(id)
    }

    //endregion

    private fun openAddStationDialog() {
        NewStationDialog().show((context as FragmentActivity).supportFragmentManager, "new_station_dialog")
    }

}

