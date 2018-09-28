package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.ui.FixedOutlineProvider
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

private const val GROUP_TITLE = 0
private const val GROUP_ITEM = 1
private const val PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE"
private const val PAYLOAD_BACKGROUND_CHANGE = "PAYLOAD_BACKGROUND_CHANGE"
private val defaultOutline = if (Build.VERSION.SDK_INT >= 21) ViewOutlineProvider.BACKGROUND else null
private val fixedOutline = if (Build.VERSION.SDK_INT >= 21) FixedOutlineProvider() else null

class StationListAdapter(private val callback: StationItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var stations = FlatStationsList()
    private var selectedStation = Station.nullObj()
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
                return stations.getId(oldItemPosition) == data.getId(newItemPosition)
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
        selectedStation = station
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTED_CHANGE)
    }

    fun setPlaying(playing: Boolean) {
        this.playing = playing
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTED_CHANGE)
    }

    fun onMove(from: Int, to: Int) {
        stations.moveItem(from, to)
        notifyItemMoved(from, to)
    }

    fun onStartDrag(position: Int) {
        setData(stations.startMove(position))
    }

    fun onIdle(): FlatStationsList {
        stations.endMove()
        return stations
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
            if (holder is GroupTitleVH) {
                val group = stations.getGroup(position)
                val selected = !group.expanded && group.id == selectedStation.groupId
                holder.select(selected, playing)
            } else {
                holder.select(stations.getStation(position).id == selectedStation.id, playing)
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
        val selected = !group.expanded && group.id == selectedStation.groupId
        holder.select(selected, playing)
    }

    private fun setupGroupItemVH(position: Int, holder: GroupItemVH) {
        val station = stations.getStation(position)

        holder.bind(station, stations.isLastStationInGroup(position))
        holder.select(station.id == selectedStation.id, playing)
        holder.setCallback(callback, station)
    }

    override fun getItemCount(): Int = stations.size
}

open class GroupElementVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var colorId = R.color.grey_50

    fun select(selected: Boolean, playing: Boolean) {
        colorId = when {
            selected && playing -> R.color.green_200
            selected -> R.color.grey_300
            else -> R.color.grey_50
        }
        setBgColor()
    }

    protected fun setBgColor() {
        itemView.background.setTintExt(itemView.context.color(colorId))
    }
}

class GroupTitleVH(itemView: View) : GroupElementVH(itemView) {
    fun bind(group: Group, callback: StationItemCallback) {
        itemView.title.text = group.getViewName(itemView.context)
        itemView.setOnClickListener { callback.onGroupSelected(group.id) }
        setExpanded(group.expanded)
    }

    private fun setExpanded(expanded: Boolean) {
        val pointer = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand
        itemView.ic_expanded.setImageResource(pointer)
        val bg = if (expanded) R.drawable.shape_item_top else R.drawable.shape_item_single
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        itemView.titleDelimiter.visible(expanded)
        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = defaultOutline
    }
}

class GroupItemVH(itemView: View) : GroupElementVH(itemView) {

    fun bind(station: Station, lastStationInGroup: Boolean) {
        itemView.name.text = station.name
        itemView.iconIv.setImageBitmap(station.icon.getBitmap(itemView.context))
        itemView.itemDelimiter.visible(!lastStationInGroup)
        val bg = if (lastStationInGroup) R.drawable.shape_item_bottom else R.drawable.shape_item_middle
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (lastStationInGroup) defaultOutline else fixedOutline
    }

    fun setCallback(callback: StationItemCallback, station: Station) {
        itemView.setOnClickListener { callback.onItemSelected(station) }
    }
}

interface StationItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(id: String)
    fun onItemOpened(station: Station)
}

