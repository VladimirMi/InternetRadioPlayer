package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.presentation.getstarted.NewStationDialog
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarView
import io.github.vladimirmi.internetradioplayer.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_media_list.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListFragment : BaseFragment(), StationListView, StationItemCallback {

    override val layoutRes = R.layout.fragment_media_list
    private val adapter = StationListAdapter(this)

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemSwipeCallback(context!!, 0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val station = adapter.getStation(viewHolder.adapterPosition) ?: return
                presenter.showStation(station)
            }
        })
    }

    @InjectPresenter lateinit var presenter: StationListPresenter

    @ProvidePresenter
    fun providePresenter(): StationListPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(StationListPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    fun notifyList() {
        adapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        media_recycler.layoutManager = LinearLayoutManager(context)
        media_recycler.adapter = adapter
        itemTouchHelper.attachToRecyclerView(media_recycler)
    }

    //region =============== StationListView ==============

    override fun setStations(stationList: FlatStationsList) {
        adapter.setData(stationList)
    }

    override fun selectItem(station: Station, playing: Boolean) {
        adapter.selectItem(station, playing)
        //todo select from position
        val position = adapter.getPosition(station)
        if (position != -1) media_recycler.scrollToPosition(position)
    }

    override fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as ToolbarView)
    }

    //endregion

    //region =============== StationItemCallback ==============

    override fun onGroupSelected(id: String) {
        presenter.selectGroup(id)
    }

    override fun onItemSelected(station: Station) {
        presenter.select(station)
    }

    override fun onItemOpened(station: Station) {
        presenter.showStation(station)
    }

    //endregion
}
