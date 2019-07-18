package io.github.vladimirmi.internetradioplayer.presentation.favorite.stations

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ContextMenu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 18.06.2019.
 */

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
        handleView.setOnTouchListener(object : View.OnTouchListener {
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

    private var isEditEnabled = false
    private var isRemoveEnabled = false
    private var isSelected = false

    override val handleView: View = itemView.titleHandleIv

    init {
        bgColorId = R.color.group
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (isEditEnabled) menu?.add(R.id.context_menu_stations, R.id.context_menu_action_edit, 0, R.string.menu_edit)
        if (isRemoveEnabled) menu?.add(R.id.context_menu_stations, R.id.context_menu_action_delete, 1, R.string.menu_delete)
    }

    fun bind(group: Group) {
        itemView.titleTv.text = Group.getViewName(group.name, itemView.context)
        setExpanded(group.expanded)
        isEditEnabled = !group.isDefault()
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
