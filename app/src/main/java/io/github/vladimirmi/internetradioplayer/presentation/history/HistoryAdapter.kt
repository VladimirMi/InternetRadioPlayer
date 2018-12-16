package io.github.vladimirmi.internetradioplayer.presentation.history

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.defaultOutline
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.fixedOutline
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

private const val PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE"
private const val PAYLOAD_BACKGROUND_CHANGE = "PAYLOAD_BACKGROUND_CHANGE"

class HistoryAdapter : RecyclerView.Adapter<StationVH>() {

    private var selectedStation: Station? = null

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
        val station = stations[position]
        when {
            payloads.contains(PAYLOAD_SELECTED_CHANGE) -> holder.select(station)
            payloads.contains(PAYLOAD_BACKGROUND_CHANGE) -> holder.setBackground(position, itemCount)
            else -> super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: StationVH, position: Int) {
        val station = stations[position]
        holder.bind(station.first)
        holder.setBackground(position, itemCount)
        holder.select(station)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(station.first) }
        holder.itemView.favoriteBt?.setOnClickListener { onAddToFavListener?.invoke(station) }
    }

    override fun getItemCount(): Int {
        return stations.size
    }

    fun selectStation(station: Station): Int {
        val oldPos = stations.indexOfFirst { it.first.uri == selectedStation?.uri }
        val newPos = stations.indexOfFirst { it.first.uri == station.uri }
        selectedStation = station
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
        return newPos
    }

    private fun StationVH.select(station: Pair<Station, Boolean>) {
        val selected = station.first.id == selectedStation?.id
        select(selected, station.second)
    }
}

class StationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTv = itemView.titleTv
    private val subtitleTv = itemView.subtitleTv
    private val favoriteBt = itemView.favoriteBt
    private val dilimeter = itemView.dilimeter
    private var bgColor: Int = 0

    @SuppressLint("SetTextI18n")
    fun bind(station: Station) {
        titleTv.text = station.name
        subtitleTv.text = station.genre
    }

    fun select(selected: Boolean, isFavorite: Boolean) {
        bgColor = itemView.context.color(if (selected) R.color.accent_light else R.color.grey_50)
        itemView.background.setTintExt(bgColor)

        favoriteBt.visible(selected || isFavorite)
        if (selected || isFavorite) {
            val tint = if (isFavorite) R.color.orange_500 else R.color.primary_light
            itemView.favoriteBt.background.setTintExt(itemView.context.color(tint))
        }
    }

    fun setBackground(position: Int, itemCount: Int) {
        var middle = false
        val bg = when {
            itemCount == 1 -> R.drawable.shape_item_single
            position == 0 -> R.drawable.shape_item_top
            position == itemCount - 1 -> R.drawable.shape_item_bottom
            else -> {
                middle = true
                R.drawable.shape_item_middle
            }
        }
        dilimeter.visible(middle || position == 0)
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        itemView.background.setTintExt(bgColor)

        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
    }
}
