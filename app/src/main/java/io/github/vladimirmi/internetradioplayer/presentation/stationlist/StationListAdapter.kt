package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.FlatStationsList
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

class StationListAdapter(private val callback: StationItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val GROUP_TITLE = 0
        const val GROUP_ITEM = 1
    }

    private var stationsList = FlatStationsList()
    private var selected: Station? = null
    private var playing = false

    fun setData(data: FlatStationsList) {
        stationsList = data
        notifyDataSetChanged()
    }

    fun getStation(position: Int): Station? {
        return if (stationsList.isGroup(position)) null
        else stationsList.getStation(position)
    }

    fun getPosition(station: Station): Int {
        return stationsList.positionOfStation(station.id)
    }

    fun selectItem(station: Station, playing: Boolean) {
        selected = station
        this.playing = playing
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
            if (stationsList.isGroup(position)) GROUP_TITLE else GROUP_ITEM

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
        val group = stationsList.getGroup(position)
        holder.bind(group, callback)
        if (!group.expanded && selected?.groupId == group.id) {
            holder.select(playing)
        } else {
            holder.unselect()
        }
    }

    private fun setupGroupItemVH(position: Int, holder: MediaGroupItemVH) {
        val station = stationsList.getStation(position)
        holder.bind(station)
        holder.setCallback(callback, station)
        if (station.uri == selected?.uri) {
            holder.select(playing)
        } else {
            holder.unselect()
        }
    }


    override fun getItemCount(): Int = stationsList.size
}

class MediaGroupTitleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(group: Group, callback: StationItemCallback) {
        itemView.title.text = group.name
        itemView.setOnClickListener { callback.onGroupSelected(group.id) }
        setExpanded(group.expanded)
    }

    fun select(playing: Boolean) {
        if (playing) itemView.setBackgroundColor(itemView.context.color(R.color.green_100))
        else itemView.setBackgroundColor(itemView.context.color(R.color.grey_300))
    }

    fun unselect() {
        itemView.setBackgroundColor(itemView.context.color(R.color.grey_50))
    }

    private fun setExpanded(expanded: Boolean) {
        val pointer = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand
        itemView.ic_expanded.setImageResource(pointer)
        val bg = if (expanded) R.drawable.shape_item_top else R.drawable.shape_item_single
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
    }
}

class MediaGroupItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(station: Station) {
        itemView.name.text = station.name
        itemView.iconIv.setImageBitmap(station.icon.getBitmap(itemView.context))
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
    fun onGroupSelected(groupId: String)
    fun onItemOpened(station: Station)
}

