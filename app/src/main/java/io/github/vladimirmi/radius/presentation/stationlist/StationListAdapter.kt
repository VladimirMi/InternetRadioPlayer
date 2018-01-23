package io.github.vladimirmi.radius.presentation.stationlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.color
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.entity.groupedlist.GroupedList
import io.github.vladimirmi.radius.model.entity.groupedlist.GroupingList
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

class MediaListAdapter(private val callback: StationItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val GROUP_TITLE = 0
        const val GROUP_ITEM = 1
    }

    private val stationInteractor = Scopes.app.getInstance(StationInteractor::class.java)
    private var stationList: GroupedList<Station> = GroupingList()
    private var selected: Station? = null
    private var playing = false

    fun setData(data: GroupedList<Station>) {
        stationList = data
        notifyDataSetChanged()
    }

    fun getStation(position: Int): Station? {
        return if (stationList.isGroupTitle(position)) null
        else stationList.getGroupItem(position)
    }

    fun selectItem(station: Station, playing: Boolean) {
        selected = station
        this.playing = playing
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
            if (stationList.isGroupTitle(position)) GROUP_TITLE else GROUP_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GROUP_TITLE -> MediaGroupTitleVH(inflater.inflate(R.layout.item_group_title, parent, false))
            GROUP_ITEM -> MediaGroupItemVH(inflater.inflate(R.layout.item_group_item, parent, false))
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MediaGroupTitleVH -> setupGroupTitleVH(position, holder)
            is MediaGroupItemVH -> setupGroupItemVH(position, holder)
        }
    }

    private fun setupGroupTitleVH(position: Int, holder: MediaGroupTitleVH) {
        val title = stationList.getGroupTitle(position)
        holder.bind(title, callback)
        holder.expanded(stationList.isGroupVisible(title))
        if (!stationList.isGroupVisible(title) && selected?.group == title) {
            holder.select(playing)
        } else {
            holder.unselect()
        }
    }

    private fun setupGroupItemVH(position: Int, holder: MediaGroupItemVH) {
        val station = stationList.getGroupItem(position)
        holder.bind(station)
        holder.setCallback(callback, station)
        if (station.uri == selected?.uri) {
            holder.select(playing)
        } else {
            holder.unselect()
        }
        val icon = stationInteractor.getIcon(station.name).blockingGet()
        holder.iconView.setImageBitmap(icon.bitmap)
    }


    override fun getItemCount(): Int = stationList.overallSize
}

class MediaGroupTitleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String, callback: StationItemCallback) {
        itemView.title.text = title
        itemView.setOnClickListener { callback.onGroupSelected(title) }
    }

    fun expanded(expanded: Boolean) {
        itemView.ic_expanded.setImageResource(if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand)
    }

    fun select(playing: Boolean) {
        if (playing) itemView.setBackgroundColor(itemView.context.color(R.color.green_100))
        else itemView.setBackgroundColor(itemView.context.color(R.color.grey_300))
    }

    fun unselect() {
        itemView.setBackgroundColor(itemView.context.color(R.color.grey_50))
    }
}

class MediaGroupItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val iconView: ImageView = itemView.iconIv

    fun bind(station: Station) {
        with(itemView) {
            name.text = station.name
            favorite.visibility = if (station.favorite) View.VISIBLE else View.INVISIBLE
        }
    }

    fun setCallback(callback: StationItemCallback, station: Station) {
        itemView.setOnClickListener { callback.onItemSelected(station) }
        itemView.setOnLongClickListener {
            callback.onItemOpened(station)
            true
        }
    }

    fun select(playing: Boolean) {
        if (playing) itemView.setBackgroundColor(itemView.context.color(R.color.green_100))
        else itemView.setBackgroundColor(itemView.context.color(R.color.grey_300))
    }

    fun unselect() {
        itemView.setBackgroundColor(itemView.context.color(R.color.grey_50))
    }
}

interface StationItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(group: String)
    fun onItemOpened(station: Station)
}

