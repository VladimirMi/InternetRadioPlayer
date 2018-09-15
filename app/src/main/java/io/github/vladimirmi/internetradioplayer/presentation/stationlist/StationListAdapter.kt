package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

private const val GROUP_TITLE = 0
private const val GROUP_ITEM = 1
private const val PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE"
private const val PAYLOAD_BACKGROUND_CHANGE = "PAYLOAD_BACKGROUND_CHANGE"

class StationListAdapter(private val callback: StationItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var stations = FlatStationsList()
    private var selectedStation = Station.nullObj()
    private var selectedPosition = 0
    private var playing = false

    fun setData(data: FlatStationsList) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return stations.size
            }

            override fun getNewListSize(): Int {
                return data.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                fun getId(list: FlatStationsList, position: Int): String {
                    return if (list.isStation(position)) list.getStation(position).id
                    else list.getGroup(position).id
                }
                return getId(stations, oldItemPosition) == getId(data, newItemPosition)
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                if (data.isGroup(newItemPosition)) {
                    if (data.getGroup(newItemPosition).expanded != stations.getGroup(oldItemPosition).expanded) {
                        return false
                    }
                }
                return true
            }
        })
        stations = data
        diffResult.dispatchUpdatesTo(this)
    }

    fun getStation(position: Int): Station? {
        return if (stations.isGroup(position)) null
        else stations.getStation(position)
    }

    fun getPosition(station: Station): Int {
        return stations.positionOfStation(station.id)
    }

    fun selectStation(station: Station) {
        val oldSelectedPos = selectedPosition
        val newSelectedPos = getPosition(station)
        selectedStation = station
        selectedPosition = newSelectedPos

        notifyItemChanged(oldSelectedPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newSelectedPos, PAYLOAD_SELECTED_CHANGE)
    }

    fun setPlaying(playing: Boolean) {
        this.playing = playing
        notifyItemChanged(selectedPosition, PAYLOAD_SELECTED_CHANGE)
    }

    override fun getItemViewType(position: Int): Int =
            if (stations.isGroup(position)) GROUP_TITLE else GROUP_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GROUP_TITLE -> GroupTitleVH(inflater.inflate(R.layout.item_group_title, parent, false))
            GROUP_ITEM -> GroupItemVH(inflater.inflate(R.layout.item_group_item, parent, false))
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder as GroupElementVH
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) {
            if (stations.isStation(position)) {
                val station = stations.getStation(position)
                holder.select(station.id == selectedStation.id, playing)
            } else {
                val group = stations.getGroup(position)
                holder.select(!group.expanded && group.id == selectedStation.groupId, playing)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupTitleVH -> setupGroupTitleVH(position, holder)
            is GroupItemVH -> setupGroupItemVH(position, holder)
        }
    }

    private fun setupGroupTitleVH(position: Int, holder: GroupTitleVH) {
        val group = stations.getGroup(position)
        holder.bind(group, callback)
        holder.select(!group.expanded && group.id == selectedStation.groupId, playing)
    }

    private fun setupGroupItemVH(position: Int, holder: GroupItemVH) {
        val station = stations.getStation(position)
        holder.bind(station)
        holder.select(station.id == selectedStation.id, playing)
        holder.setCallback(callback, station)
    }


    override fun getItemCount(): Int = stations.size
}

open class GroupElementVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun select(selected: Boolean, playing: Boolean) {
        val colorId = when {
            playing -> R.color.green_200
            selected -> R.color.grey_300
            else -> R.color.grey_50
        }
        itemView.setBackgroundColor(itemView.context.color(colorId))
    }
}


class GroupTitleVH(itemView: View) : GroupElementVH(itemView) {
    fun bind(group: Group, callback: StationItemCallback) {
        itemView.title.text = group.name
        itemView.setOnClickListener { callback.onGroupSelected(group.id) }
        setExpanded(group.expanded)
    }

    private fun setExpanded(expanded: Boolean) {
        val pointer = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand
        itemView.ic_expanded.setImageResource(pointer)
        val bg = if (expanded) R.drawable.shape_item_top else R.drawable.shape_item_single
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
    }
}

class GroupItemVH(itemView: View) : GroupElementVH(itemView) {

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
}

interface StationItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(id: String)
    fun onItemOpened(station: Station)
}

