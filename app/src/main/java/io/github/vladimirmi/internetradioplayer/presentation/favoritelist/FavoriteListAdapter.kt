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
    : RecyclerView.Adapter<GroupElementVH>() {

    private var stations = FlatStationsList()
    private var selectedStation = Station.nullObj()

    fun setData(data: FlatStationsList) {
        val diffResult = FavoriteListDiff(stations, data).calc()
        stations = data
        diffResult.dispatchUpdatesTo(this)
        notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND_CHANGE)
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
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: GroupElementVH, position: Int) {
        holder.changeBackground(stations, position)
        holder.select(position)
        when (holder) {
            is GroupTitleVH -> setupGroupTitleVH(position, holder)
            is GroupItemVH -> setupGroupItemVH(position, holder)
        }
    }

    private fun setupGroupTitleVH(position: Int, holder: GroupTitleVH) {
        val group = stations.getGroup(position)
        holder.bind(group)
        holder.itemView.setOnClickListener { callback.onGroupSelected(group.id) }
    }

    private fun setupGroupItemVH(position: Int, holder: GroupItemVH) {
        val station = stations.getStation(position)
        holder.bind(station)
        holder.itemView.setOnClickListener { callback.onItemSelected(station) }
    }

    override fun getItemCount(): Int = stations.size

    private fun GroupElementVH.select(position: Int) {
        val selected = if (stations.isGroup(position)) {
            val group = stations.getGroup(position)
            !group.expanded && group.id == selectedStation.groupId
        } else {
            val station = stations.getStation(position)
            station.id == selectedStation.id
        }
        select(selected)
    }
}

abstract class GroupElementVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var colorId = R.color.grey_50

    fun select(selected: Boolean) {
        colorId = when {
            selected -> R.color.grey_300
            else -> R.color.grey_50
        }
        setBgColor()
    }

    protected fun setMargins(addBottomMargin: Boolean) {
        val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
        lp.bottomMargin = (if (addBottomMargin) 8 else 0) * itemView.context.dp
        itemView.layoutParams = lp
    }

    protected fun setBgColor() {
        itemView.background?.setTintExt(itemView.context.color(colorId))
    }

    abstract fun changeBackground(stations: FlatStationsList, position: Int)
}

class GroupTitleVH(itemView: View) : GroupElementVH(itemView) {

    fun bind(group: Group) {
        itemView.title.text = Group.getViewName(group.name, itemView.context)
        setExpanded(group.expanded)
    }

    private fun setExpanded(expanded: Boolean) {
        val pointer = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand
        itemView.iconExpandedIv.setImageResource(pointer)
    }

    override fun changeBackground(stations: FlatStationsList, position: Int) {
        val group = stations.getGroup(position)
        val bg = if (group.expanded) R.drawable.shape_item_top else R.drawable.shape_item_single
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        if (Build.VERSION.SDK_INT >= 21) {
            itemView.outlineProvider = defaultOutline
        }
        setMargins(!group.expanded && position != stations.size - 1)
    }
}

class GroupItemVH(itemView: View) : GroupElementVH(itemView) {

    fun bind(station: Station) {
        itemView.name.text = station.name
    }

    override fun changeBackground(stations: FlatStationsList, position: Int) {
        val top = position == 0
        val bottom = stations.isLastStationInGroup(position)
        val single = top && bottom
        val middle = !top && !bottom && !single
        val bg = when {
            top -> R.drawable.shape_item_top
            middle -> R.drawable.shape_item_middle
            bottom -> R.drawable.shape_item_bottom
            single -> R.drawable.shape_item_single
            else -> throw IllegalStateException()
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setBgColor()
        if (Build.VERSION.SDK_INT >= 21) {
            itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
        }
        setMargins(bottom && position != stations.size - 1)
    }
}

interface StationItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(id: String)
    fun onItemOpened(station: Station)
}
