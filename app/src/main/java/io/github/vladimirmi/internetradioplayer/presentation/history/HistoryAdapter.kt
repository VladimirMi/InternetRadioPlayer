package io.github.vladimirmi.internetradioplayer.presentation.history

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_station.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class HistoryAdapter : RecyclerView.Adapter<HistoryVH>() {

    private var selectedUri: String? = null

    var longClickedItem: Station? = null
    var onItemClickListener: ((Station) -> Unit)? = null

    var data: List<Station> = emptyList()
        set(value) {
            val diff = getDiff(field, value)
            field = value
            diff.dispatchUpdatesTo(this)
            notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND_CHANGE)
        }

    private fun getDiff(old: List<Station>, new: List<Station>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition].uri == new[newItemPosition].uri
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition].isFavorite == new[newItemPosition].isFavorite
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return PAYLOAD_FAVORITE_CHANGE
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryVH(view)
    }

    override fun onBindViewHolder(holder: HistoryVH, position: Int, payloads: MutableList<Any>) {
        val media = data[position]
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) holder.select(media.uri == selectedUri)
        if (payloads.contains(PAYLOAD_BACKGROUND_CHANGE)) holder.setBackground(position, itemCount)
        if (payloads.contains(PAYLOAD_FAVORITE_CHANGE)) holder.setFavorite(media.isFavorite)
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)

    }

    override fun onBindViewHolder(holder: HistoryVH, position: Int) {
        val media = data[position]
        holder.bind(media)
        holder.setFavorite(media.isFavorite)
        holder.select(media.uri == selectedUri)
        holder.setBackground(position, itemCount)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(media) }
        holder.itemView.setOnLongClickListener { longClickedItem = media; false }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun selectMedia(id: String): Int {
        val oldPos = data.indexOfFirst { it.uri == selectedUri }
        val newPos = data.indexOfFirst { it.uri == id }
        selectedUri = id
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
        return newPos
    }
}

class HistoryVH(itemView: View) : RecyclerView.ViewHolder(itemView)
        , View.OnCreateContextMenuListener, LayoutContainer {

    private var bgColor: Int = 0

    init {
        itemView.setOnCreateContextMenuListener(this)
    }

    override val containerView: View?
        get() = itemView

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(R.id.context_menu_history, R.id.context_menu_action_delete, 0, R.string.menu_delete)
    }

    fun bind(media: Media) {
        titleTv.text = media.name
        subtitleTv.text = media.specs
    }

    fun setFavorite(isFavorite: Boolean) {
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(titleTv, 0, 0,
                if (isFavorite) R.drawable.ic_star else 0, 0)
    }

    fun select(selected: Boolean) {
        bgColor = itemView.context.color(if (selected) R.color.accent_light else R.color.grey_50)
        (itemView.background as? GradientDrawable)?.setColor(bgColor)
    }

    fun setBackground(position: Int, itemCount: Int) {
        var middle = false
        val bg = when {
            itemCount == 1 -> R.drawable.bg_item_single
            position == 0 -> R.drawable.bg_item_top
            position == itemCount - 1 -> R.drawable.bg_item_bottom
            else -> {
                middle = true
                R.drawable.bg_item_middle
            }
        }
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        (itemView.background as? GradientDrawable)?.setColor(bgColor)

        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
    }
}
