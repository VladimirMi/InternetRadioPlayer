package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.*
import android.view.View.OnTouchListener
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.dp
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.ui.FixedOutlineProvider
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*


/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

const val PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE"
const val PAYLOAD_BACKGROUND_CHANGE = "PAYLOAD_BACKGROUND_CHANGE"
const val PAYLOAD_DRAG_MODE_CHANGE = "PAYLOAD_DRAG_MODE_CHANGE"

private const val GROUP_TITLE = 0
private const val GROUP_ITEM = 1

val defaultOutline = if (Build.VERSION.SDK_INT >= 21) ViewOutlineProvider.BACKGROUND else null
val fixedOutline = if (Build.VERSION.SDK_INT >= 21) FixedOutlineProvider() else null

class StationListAdapter(private val callback: FavoriteStationsAdapterCallback)
    : RecyclerView.Adapter<GroupElementVH>() {

    var longClickedItem: Any? = null

    private var stations = FlatStationsList()
    private var dragEnabled = false
    private var dragged = false
    private var selectedStation: Station? = null

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

    fun getPosition(station: Station): Int {
        return stations.positionOfStation(station.id)
    }

    fun selectStation(station: Station) {
        selectedStation = station
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
        holder.itemView.setOnLongClickListener { longClickedItem = stations[position]; false }
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
        holder.itemView.setOnClickListener { callback.onGroupSelected(group.id) }
    }

    private fun setupGroupItemVH(position: Int, holder: GroupItemVH) {
        val station = stations.getStation(position)
        holder.bind(station)
        holder.itemView.setOnClickListener { callback.onItemSelected(station) }
    }

    private fun GroupElementVH.select(position: Int) {
        val selected = if (stations.isGroup(position)) {
            val group = stations.getGroup(position)
            !group.expanded && group.stations.find { selectedStation?.uri == it.uri } != null
        } else {
            val station = stations.getStation(position)
            station.id == selectedStation?.id
        }
        select(selected)
    }
}

//todo Refactor: move to separate kotlin file
abstract class GroupElementVH(itemView: View)
    : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

    @ColorRes protected var bgColorId = R.color.grey_50

    init {
        @Suppress("LeakingThis")
        itemView.setOnCreateContextMenuListener(this)
    }

    abstract val handleView: View

    abstract fun select(selected: Boolean)

    abstract fun changeBackground(stations: FlatStationsList, position: Int)

    abstract fun setDragEnabled(dragEnabled: Boolean)

    protected fun setBottomMargin(addBottomMargin: Boolean) {
        val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams
        lp.bottomMargin = (if (addBottomMargin) 16 else 0) * itemView.context.dp
        itemView.layoutParams = lp
    }

    protected fun setupBgColor() {
        (itemView.background as? GradientDrawable)?.setColor(itemView.context.color(bgColorId))
    }

    fun setDragListener(callback: FavoriteStationsAdapterCallback) {
        handleView.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    callback.onStartDrag(this@GroupElementVH)
                    v.performClick()
                }
                return false
            }
        })
    }
}

class GroupTitleVH(itemView: View) : GroupElementVH(itemView) {

    private var isRemoveEnabled = false
    private var isSelected = false

    override val handleView: View = itemView.titleHandleIv

    init {
        bgColorId = R.color.group
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(R.id.context_menu_stations, R.id.context_menu_action_edit, 0, R.string.menu_edit)
        if (isRemoveEnabled) menu?.add(R.id.context_menu_stations, R.id.context_menu_action_delete, 1, R.string.menu_delete)
    }

    fun bind(group: Group) {
        itemView.titleTv.text = Group.getViewName(group.name, itemView.context)
        setExpanded(group.expanded)
        isRemoveEnabled = group.stations.isEmpty()
    }

    private fun setExpanded(expanded: Boolean) {
        val pointer = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand
        itemView.iconExpandedIv.setImageResource(pointer)
    }

    override fun select(selected: Boolean) {
        isSelected = selected
        itemView.selectionView.visible(selected)
    }

    override fun changeBackground(stations: FlatStationsList, position: Int) {
        val group = stations.getGroup(position)
        val single = !group.expanded || group.stations.isEmpty()

        val bg = if (single) R.drawable.bg_item_single else R.drawable.bg_item_top
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setupBgColor()
        if (Build.VERSION.SDK_INT >= 21) {
            itemView.outlineProvider = defaultOutline
        }
        setBottomMargin(single && position != stations.size - 1)
    }

    override fun setDragEnabled(dragEnabled: Boolean) {
        itemView.selectionView.visible(!dragEnabled && isSelected)
        handleView.visible(dragEnabled)
    }
}

class GroupItemVH(itemView: View) : GroupElementVH(itemView), View.OnCreateContextMenuListener {

    override val handleView: View = itemView.handleIv

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(R.id.context_menu_stations, R.id.context_menu_action_edit, 0, R.string.menu_edit)
        menu?.add(R.id.context_menu_stations, R.id.context_menu_action_delete, 1, R.string.menu_delete)
    }

    fun bind(station: Station) {
        itemView.nameTv.text = station.name
        itemView.specsTv.text = station.specs
    }

    override fun select(selected: Boolean) {
        bgColorId = when {
            selected -> R.color.accent_light
            else -> R.color.grey_50
        }
        setupBgColor()
    }

    override fun changeBackground(stations: FlatStationsList, position: Int) {
        val top = position == 0
        val bottom = stations.isLastStationInGroup(position)
        val single = top && bottom
        val middle = !top && !bottom && !single
        val bg = when {
            single -> R.drawable.bg_item_single
            top -> R.drawable.bg_item_top
            middle -> R.drawable.bg_item_middle
            bottom -> R.drawable.bg_item_bottom
            else -> throw IllegalStateException()
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        setupBgColor()
        if (Build.VERSION.SDK_INT >= 21) {
            itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
        }
        setBottomMargin(bottom && position != stations.size - 1)
    }

    override fun setDragEnabled(dragEnabled: Boolean) {
        handleView.visible(dragEnabled)
    }
}

interface FavoriteStationsAdapterCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(id: String)
    fun onStartDrag(vh: RecyclerView.ViewHolder)
}
