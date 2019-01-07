package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.dp
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.main.SimpleControlsView
import kotlinx.android.synthetic.main.fragment_favorite_list.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoriteListFragment : BaseFragment<FavoriteListPresenter, StationListView>(), StationListView,
        StationItemCallback, SimpleControlsView {

    override val layout = R.layout.fragment_favorite_list

    private val adapter by lazy { StationListAdapter(this) }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemSwipeCallback() {

            override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                                target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
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


    override fun providePresenter(): FavoriteListPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(FavoriteListPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
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

    override fun showControls(visibility: Float) {
        val pb = ((48 * (1 - visibility) + 16) * context!!.dp).toInt()
        stationsRv.setPadding(0, stationsRv.paddingTop, 0, pb)
    }

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
