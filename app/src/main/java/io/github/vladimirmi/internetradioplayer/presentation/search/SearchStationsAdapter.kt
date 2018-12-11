package io.github.vladimirmi.internetradioplayer.presentation.search

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
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

class SearchStationsAdapter : RecyclerView.Adapter<SearchStationVH>() {

    private var selectedStation: StationSearchRes? = null
    private var favorites = FlatStationsList()

    var onItemClickListener: ((StationSearchRes) -> Unit)? = null

    var onAddToFavListener: ((StationSearchRes) -> Unit)? = null

    var stations: List<StationSearchRes> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchStationVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_station, parent, false)
        return SearchStationVH(view)
    }

    override fun onBindViewHolder(holder: SearchStationVH, position: Int, payloads: MutableList<Any>) {
        val station = stations[position]
        when {
            payloads.contains(PAYLOAD_SELECTED_CHANGE) -> holder.select(station)
            else -> super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: SearchStationVH, position: Int) {
        val station = stations[position]
        holder.bind(station)
        holder.setBackground(position, itemCount)
        holder.select(station)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(station) }
        holder.itemView.favoriteBt?.setOnClickListener { onAddToFavListener?.invoke(station) }
    }

    override fun getItemCount(): Int {
        return stations.size
    }

    fun selectStation(station: Station) {
        val oldPos = stations.indexOf(selectedStation)
        val newPos = stations.indexOfFirst { it.uri == station.uri }
        selectedStation = if (newPos == -1) null else stations[newPos]
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
    }

    fun setFavorites(favorites: FlatStationsList) {
        this.favorites = favorites
        notifyDataSetChanged()
    }

    private fun SearchStationVH.select(station: StationSearchRes) {
        val isFavorite = favorites.findStation { it.uri == station.uri } != null
        val selected = station.id == selectedStation?.id
        select(selected, isFavorite)
    }
}

class SearchStationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTv = itemView.titleTv
    private val subtitleTv = itemView.subtitleTv
    private val favoriteBt = itemView.favoriteBt
    private val dilimeter = itemView.dilimeter

    @SuppressLint("SetTextI18n")
    fun bind(res: StationSearchRes) {
        titleTv.text = res.callsign
        subtitleTv.text = "${res.artist} - ${res.title}"
    }

    fun select(selected: Boolean, isFavorite: Boolean) {
        val bg = itemView.context.color(if (selected) R.color.accent_light else R.color.grey_50)
        itemView.background.setTintExt(bg)

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

        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
    }
}
