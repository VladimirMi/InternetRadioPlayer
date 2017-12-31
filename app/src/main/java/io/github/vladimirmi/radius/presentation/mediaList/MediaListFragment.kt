package io.github.vladimirmi.radius.presentation.mediaList

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_media_list.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaListFragment : BaseFragment(), MediaListView, MediaItemCallback {

    override val layoutRes = R.layout.fragment_media_list
    private val adapter = MediaListAdapter(this)

    private val itemTouchHelper = ItemTouchHelper(object : ItemSwipeCallback(Scopes.context,
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val station = adapter.getStation(viewHolder.adapterPosition) ?: return
            if (direction == ItemTouchHelper.LEFT) {
                RemoveDialog.newInstance(station).show(childFragmentManager, "remove_dialog")
            } else {
                presenter.showStation(station)
            }
        }
    })

    @InjectPresenter lateinit var presenter: MediaListPresenter

    @ProvidePresenter
    fun providePresenter(): MediaListPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MediaListPresenter::class.java).also {
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

    override fun setMediaList(stationList: GroupedList<Station>) {
        adapter.setData(stationList)
    }

    override fun onGroupSelected(group: String) {
        presenter.selectGroup(group)
    }

    override fun onItemSelected(station: Station) {
        presenter.select(station)
    }

    override fun selectItem(station: Station, playing: Boolean) {
        adapter.selectItem(station, playing)
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as RootActivity)
    }
}
