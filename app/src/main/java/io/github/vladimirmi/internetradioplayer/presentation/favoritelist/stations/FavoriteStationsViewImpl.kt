package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFrameView
import kotlinx.android.synthetic.main.view_favorite_stations.view.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class FavoriteStationsViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseFrameView<FavoriteStationsPresenter, FavoriteStationsView>(context, attrs, defStyleAttr),
        FavoriteStationsView, StationItemCallback {

    private val adapter by lazy { StationListAdapter(this) }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemSwipeCallback() {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onStartDrag(position: Int) {
                adapter.onStartDrag(position)
            }

            override fun onIdle() {
                presenter.moveGroupElements(adapter.onIdle())
            }
        })
    }

    override fun providePresenter(): FavoriteStationsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(FavoriteStationsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
        stationsRv.layoutManager = LinearLayoutManager(context!!)
        stationsRv.adapter = adapter
        itemTouchHelper.attachToRecyclerView(stationsRv)
    }


    //region =============== StationListView ==============

    override fun setStations(stationList: FlatStationsList) {
        adapter.setData(stationList)
    }

    override fun selectStation(station: Station) {
        adapter.selectStation(station)
        val position = adapter.getPosition(station)
        if (position != -1) stationsRv.scrollToPosition(position)
    }

//    override fun showControls(visibility: Float) {
//        val pb = ((48 * (1 - visibility) + 16) * context!!.dp).toInt()
//        stationsRv.setPadding(0, stationsRv.paddingTop, 0, pb)
//    }

    override fun showPlaceholder(show: Boolean) {
        stationsRv.visible(!show)
        placeholderView.visible(show)
    }

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
}