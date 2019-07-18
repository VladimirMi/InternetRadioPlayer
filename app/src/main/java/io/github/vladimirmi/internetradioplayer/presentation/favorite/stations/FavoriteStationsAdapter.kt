package io.github.vladimirmi.internetradioplayer.presentation.favorite.stations

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.ui.FixedOutlineProvider


/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

const val PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE"
const val PAYLOAD_FAVORITE_CHANGE = "PAYLOAD_FAVORITE_CHANGE"
const val PAYLOAD_BACKGROUND_CHANGE = "PAYLOAD_BACKGROUND_CHANGE"
const val PAYLOAD_DRAG_MODE_CHANGE = "PAYLOAD_DRAG_MODE_CHANGE"

private const val GROUP_TITLE = 0
private const val GROUP_ITEM = 1

val defaultOutline = if (Build.VERSION.SDK_INT >= 21) ViewOutlineProvider.BACKGROUND else null
val fixedOutline = if (Build.VERSION.SDK_INT >= 21) FixedOutlineProvider() else null

class FavoriteStationAdapter(private val callback: FavoriteStationsAdapterCallback)
    : RecyclerView.Adapter<GroupElementVH>() {

    var longClickedItem: Any? = null

    private var stations = FlatStationsList()
    private var dragEnabled = false
    private var dragged = false
    private var selectedStationUri: String? = null

    fun setData(data: FlatStationsList) {
        if (dragged) {
            stations = data
            notifyDataSetChanged()
            dragged = false
        } else {
            val diffResult = FavoriteStationsDiff(stations, data).calc()
            stations = data
            diffResult.dispatchUpdatesTo(this)
            notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND_CHANGE)
        }
    }

    fun getPosition(id: String): Int {
        return stations.positionOfStation(id)
    }

    fun selectStation(uri: String) {
        selectedStationUri = uri
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTED_CHANGE)
    }

    fun setDragEnabled(enabled: Boolean) {
        dragEnabled = enabled
        notifyItemRangeChanged(0, itemCount, PAYLOAD_DRAG_MODE_CHANGE)
    }

    fun onMove(from: Int, to: Int) {
        stations.moveItem(from, to)
        notifyItemMoved(from, to)
        notifyItemChanged(from, PAYLOAD_BACKGROUND_CHANGE)
        notifyItemChanged(to, PAYLOAD_BACKGROUND_CHANGE)
    }

    fun onStartDrag(position: Int) {
        setData(stations.startMove(position))
        dragged = true
    }

    fun onIdle(): FlatStationsList {
        return stations.endMove()
    }

    override fun getItemViewType(position: Int): Int =
            if (stations.isGroup(position)) GROUP_TITLE else GROUP_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupElementVH {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GROUP_TITLE -> GroupTitleVH(inflater.inflate(R.layout.item_group_title, parent, false))
            GROUP_ITEM -> GroupItemVH(inflater.inflate(R.layout.item_group_item, parent, false))
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: GroupElementVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_BACKGROUND_CHANGE)) holder.changeBackground(stations, position)
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) holder.select(position)
        if (payloads.contains(PAYLOAD_DRAG_MODE_CHANGE)) holder.setDragEnabled(dragEnabled)
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: GroupElementVH, position: Int) {
        val station = stations[position]
        holder.itemView.setOnLongClickListener { longClickedItem = station; false }
        holder.changeBackground(stations, position)
        holder.select(position)
        holder.setDragListener(callback)
        holder.setDragEnabled(dragEnabled)
        when (holder) {
            is GroupTitleVH -> setupGroupTitleVH(position, holder)
            is GroupItemVH -> setupGroupItemVH(position, holder)
        }
    }

    override fun getItemCount(): Int = stations.size

    private fun setupGroupTitleVH(position: Int, holder: GroupTitleVH) {
        val group = stations.getGroup(position)
        holder.bind(group)
        holder.itemView.setOnClickListener { callback.onGroupSelected(group) }
    }

    private fun setupGroupItemVH(position: Int, holder: GroupItemVH) {
        val station = stations.getStation(position)
        holder.bind(station)
        holder.itemView.setOnClickListener { callback.onItemSelected(station) }
    }

    private fun GroupElementVH.select(position: Int) {
        val selected = if (stations.isGroup(position)) {
            val group = stations.getGroup(position)
            !group.expanded && group.stations.find { selectedStationUri == it.uri } != null
        } else {
            val station = stations.getStation(position)
            station.uri == selectedStationUri
        }
        select(selected)
    }
}

interface FavoriteStationsAdapterCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(group: Group)
    fun onStartDrag(vh: RecyclerView.ViewHolder)
}
