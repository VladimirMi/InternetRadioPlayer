package io.github.vladimirmi.internetradioplayer.presentation.history

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.PAYLOAD_BACKGROUND_CHANGE
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.PAYLOAD_SELECTED_CHANGE
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.defaultOutline
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.fixedOutline
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class HistoryAdapter : RecyclerView.Adapter<StationVH>() {

    private var selectedStationUri: String? = null

    var longClickedItem: Station? = null

    var onItemClickListener: ((Station) -> Unit)? = null
    var onAddToFavListener: ((Pair<Station, Boolean>) -> Unit)? = null

    var stations: List<Pair<Station, Boolean>> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size

                override fun getNewListSize() = value.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].first.id == value[newItemPosition].first.id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition] == value[newItemPosition]
                }
            })
            field = value
            diff.dispatchUpdatesTo(this)

            notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND_CHANGE)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_station, parent, false)
        return StationVH(view)
    }

    override fun onBindViewHolder(holder: StationVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) holder.select(stations[position])
        if (payloads.contains(PAYLOAD_BACKGROUND_CHANGE)) holder.setBackground(position, itemCount)
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)

    }

    override fun onBindViewHolder(holder: StationVH, position: Int) {
        val station = stations[position]
        holder.bind(station.first)
        holder.select(station)
        holder.setBackground(position, itemCount)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(station.first) }
        holder.itemView.setOnLongClickListener { longClickedItem = station.first; false }
    }

    override fun getItemCount(): Int {
        return stations.size
    }

    fun selectStation(uri: String): Int {
        val oldPos = stations.indexOfFirst { it.first.uri == selectedStationUri }
        val newPos = stations.indexOfFirst { it.first.uri == uri }
        selectedStationUri = uri
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
        return newPos
    }

    private fun StationVH.select(station: Pair<Station, Boolean>) {
        val selected = station.first.uri == selectedStationUri
        select(selected, station.second)
        if (selected) {
            itemView.favoriteBt?.setOnClickListener { onAddToFavListener?.invoke(station) }
        } else {
            itemView.favoriteBt?.setOnClickListener { onItemClickListener?.invoke(station.first) }
        }
    }
}

class StationVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
    private val titleTv = itemView.titleTv

    private val subtitleTv = itemView.subtitleTv
    private val favoriteBt = itemView.favoriteBt
    private var bgColor: Int = 0

    init {
        itemView.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(R.id.context_menu_history, R.id.context_menu_action_delete, 0, R.string.menu_delete)
    }

    @SuppressLint("SetTextI18n")
    fun bind(station: Station) {
        titleTv.text = station.name
        subtitleTv.text = station.specs
    }

    fun select(selected: Boolean, isFavorite: Boolean) {
        bgColor = itemView.context.color(if (selected) R.color.accent_light else R.color.grey_50)
        (itemView.background as? GradientDrawable)?.setColor(bgColor)

        favoriteBt.visible(selected || isFavorite)
        if (selected || isFavorite) {
            val tint = if (isFavorite) R.color.orange_500 else R.color.primary_variant
            itemView.favoriteBt.setColorFilter(itemView.context.color(tint))
        }
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
