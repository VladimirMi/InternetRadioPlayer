package io.github.vladimirmi.internetradioplayer.presentation.favorite.stations

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
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
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

private const val EDIT_STATION_DIALOG = "edit_station_dialog"
private const val EDIT_GROUP_DIALOG = "edit_group_dialog"
private const val NEW_GROUP_DIALOG = "new_group_dialog"
private const val NEW_STATION_DIALOG = "new_station_dialog"

class FavoriteStationsFragment : BaseFragment<FavoriteStationsPresenter, FavoriteStationsView>(),
        FavoriteStationsView, FavoriteStationsAdapterCallback, EditDialog.Callback {

    override val layout = R.layout.fragment_favorite_stations

    private val stationListAdapter by lazy { FavoriteStationAdapter(this) }
    private var changeOrderMode = false
    private var enableMenu = false

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
        itemTouchHelper.attachToRecyclerView(stationsRv)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (!userVisibleHint || !enableMenu) return
        menu.clear()
        if (changeOrderMode) {
            menu.add(Menu.NONE, R.string.menu_change_order, Menu.NONE, R.string.menu_change_order).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                icon = ContextCompat.getDrawable(context!!, R.drawable.ic_submit)
            }
        } else {
            menu.add(Menu.NONE, R.string.menu_change_order, Menu.NONE, R.string.menu_change_order).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                icon = ContextCompat.getDrawable(context!!, R.drawable.ic_sort)
            }
        }
        menu.add(Menu.NONE, R.string.menu_add_group, Menu.NONE, R.string.menu_add_group).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            icon = ContextCompat.getDrawable(context!!, R.drawable.ic_add)
        }
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_change_order -> switchChangeOrderMode()
            R.string.menu_add_group -> openAddGroupDialog()
            else -> return false
        }
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId != R.id.context_menu_stations) return false
        when (val selectedItem = stationListAdapter.longClickedItem) {
            is Station -> when {
                item.itemId == R.id.context_menu_action_edit -> openStationEditDialog(selectedItem)
                item.itemId == R.id.context_menu_action_delete -> presenter.deleteStation(selectedItem)
                else -> return false
            }
            is Group -> when {
                item.itemId == R.id.context_menu_action_edit -> openGroupEditDialog(selectedItem)
                item.itemId == R.id.context_menu_action_delete -> presenter.deleteGroup(selectedItem)
                else -> return false
            }
            else -> return false
        }
        return true
    }

    override fun handleBackPressed(): Boolean {
        return if (changeOrderMode) {
            switchChangeOrderMode(); true
        } else {
            false
        }
    }

    //region =============== StationListView ==============

    override fun setStations(stationList: FlatStationsList) {
        stationListAdapter.setData(stationList)
    }

    override fun selectStation(uri: String) {
        stationListAdapter.selectStation(uri)
        val position = stationListAdapter.getPosition(uri)
        if (position != -1) stationsRv.scrollToPosition(position)
    }

    override fun showPlaceholder(show: Boolean) {
        stationsRv.visible(!show)
        placeholderView.visible(show)
        enableMenu = !show
        activity?.invalidateOptionsMenu()
    }

    //endregion

    //region =============== FavoriteStationsAdapterCallback ==============

    override fun onGroupSelected(group: Group) {
        presenter.selectGroup(group)
    }

    override fun onItemSelected(station: Station) {
        presenter.selectStation(station)
    }

    override fun onStartDrag(vh: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(vh)
    }

    //endregion

    override fun onDialogEdit(newText: String, tag: String) {
        when (tag) {
            EDIT_STATION_DIALOG -> presenter.editStation(stationListAdapter.longClickedItem as Station, newText)
            EDIT_GROUP_DIALOG -> presenter.editGroup(stationListAdapter.longClickedItem as Group, newText)
            NEW_GROUP_DIALOG -> presenter.createGroup(Group.getDbName(newText, requireContext()))
        }
    }

    private fun openStationEditDialog(station: Station) {
        EditDialog.newInstance(getString(R.string.dialog_edit_station),
                getString(R.string.title), station.name)
                .show(childFragmentManager, EDIT_STATION_DIALOG)
    }

    private fun openAddStationDialog() {
        NewStationDialog().show((context as FragmentActivity).supportFragmentManager, NEW_STATION_DIALOG)
    }

    private fun openAddGroupDialog() {
        EditDialog.newInstance(getString(R.string.dialog_new_group),
                getString(R.string.title), "")
                .show(childFragmentManager, NEW_GROUP_DIALOG)
    }

    private fun openGroupEditDialog(group: Group) {
        EditDialog.newInstance(getString(R.string.dialog_edit_group),
                getString(R.string.title), group.name)
                .show(childFragmentManager, EDIT_GROUP_DIALOG)
    }

    private fun switchChangeOrderMode() {
        changeOrderMode = !changeOrderMode
        addStationFab.visible(!changeOrderMode)
        stationListAdapter.setDragEnabled(changeOrderMode)
        activity?.invalidateOptionsMenu()
    }

}

