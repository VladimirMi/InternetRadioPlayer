package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.dp
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
val defaultOutline = if (Build.VERSION.SDK_INT >= 21) ViewOutlineProvider.BACKGROUND else null
val fixedOutline = if (Build.VERSION.SDK_INT >= 21) FixedOutlineProvider() else null

class StationListAdapter(private val callback: StationItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var stations = FlatStationsList()
    private var selectedStation = Station.nullObj()

    fun setData(data: FlatStationsList) {
        val diffResult = FavoriteListDiff(stations, data).calc()
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

    fun onMove(from: Int, to: Int) {
        stations.moveItem(from, to)
        notifyItemMoved(from, to)
        notifyItemChanged(from, PAYLOAD_BACKGROUND_CHANGE)
        notifyItemChanged(to, PAYLOAD_BACKGROUND_CHANGE)
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
                holder.select(selected)
            } else {
                holder.select(stations.getStation(position).id == selectedStation.id)
            }
        } else if (payloads.contains(PAYLOAD_BACKGROUND_CHANGE)) {
            holder.setMargins(position == 0 || holder is GroupTitleVH, position == itemCount - 1)
            (holder as? GroupItemVH)?.changeBackground(stations.isLastStationInGroup(position),
                    position == 0)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GroupElementVH).setMargins(position == 0 || holder is GroupTitleVH,
                position == itemCount - 1)
        when (holder) {
            is GroupTitleVH -> setupGroupTitleVH(position, holder)
            is GroupItemVH -> setupGroupItemVH(position, holder)
        }
    }

    private fun setupGroupTitleVH(position: Int, holder: GroupTitleVH) {
        val group = stations.getGroup(position)
        holder.bind(group)
        holder.itemView.setOnClickListener { callback.onGroupSelected(group.id) }
        val selected = !group.expanded && group.id == selectedStation.groupId
        holder.select(selected)
    }

    private fun setupGroupItemVH(position: Int, holder: GroupItemVH) {
        val station = stations.getStation(position)

        holder.bind(station)
        holder.changeBackground(stations.isLastStationInGroup(position), position == 0)
        holder.changeBackground(stations, position)
        holder.select(station.id == selectedStation.id)
        holder.itemView.setOnClickListener { callback.onItemSelected(station) }
    }

    override fun getItemCount(): Int = stations.size
}

open class GroupElementVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var colorId = R.color.grey_50

    fun select(selected: Boolean) {
        colorId = when {
            selected -> R.color.grey_300
            else -> R.color.grey_50
        }
        setBgColor()
    }

    fun setMargins(addTopMargin: Boolean, addBottomMargin: Boolean) {
        val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = (if (addTopMargin) 16 else 0) * itemView.context.dp
        lp.bottomMargin = (if (addBottomMargin) 16 else 0) * itemView.context.dp
        itemView.layoutParams = lp
    }

    protected fun setBgColor() {
        itemView.background.setTintExt(itemView.context.color(colorId))
    }
}

class GroupTitleVH(itemView: View) : GroupElementVH(itemView) {

    fun bind(group: Group) {
        itemView.title.text = Group.getViewName(group.name, itemView.context)
        setExpanded(group.expanded)
    }

    private fun setExpanded(expanded: Boolean) {
        val pointer = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand
        itemView.iconExpandedIv.setImageResource(pointer)
        val bg = if (expanded) R.drawable.shape_item_top else R.drawable.shape_item_single
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        itemView.titleDelimiter.visible(expanded)
        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = defaultOutline
    }
}

class GroupItemVH(itemView: View) : GroupElementVH(itemView) {

    fun bind(station: Station) {
        itemView.name.text = station.name
    }

    fun changeBackground(stations: FlatStationsList, position: Int) {
        var middle = false
        val bg = if (position == 0 && stations.size > 1) {
            R.drawable.shape_item_top
        } else if (stations.isLastStationInGroup(position) && stations.size > 1) {
            R.drawable.shape_item_bottom
        } else {
            middle = true
            R.drawable.shape_item_middle
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
    }

    fun changeBackground(lastStationInGroup: Boolean, firstInList: Boolean) {
        itemView.itemDelimiter.visible(!lastStationInGroup)
        val bg = when {
            lastStationInGroup -> R.drawable.shape_item_bottom
            firstInList -> R.drawable.shape_item_top
            else -> R.drawable.shape_item_middle
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (lastStationInGroup || firstInList) defaultOutline
        else fixedOutline
    }
}

interface StationItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(id: String)
    fun onItemOpened(station: Station)
}

