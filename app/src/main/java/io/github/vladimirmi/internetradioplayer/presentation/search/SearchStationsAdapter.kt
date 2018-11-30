package io.github.vladimirmi.internetradioplayer.presentation.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

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

    override fun onBindViewHolder(holder: SearchStationVH, position: Int) {
        val station = stations[position]
        val isFavorite = favorites.findStation { it.uri == station.uri } != null
        holder.bind(station)
        holder.select(station.id == selectedStation?.id, isFavorite)
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
        notifyItemChanged(oldPos)
        notifyItemChanged(newPos)
    }

    fun setFavorites(favorites: FlatStationsList) {
        this.favorites = favorites
        notifyDataSetChanged()
    }
}

class SearchStationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @SuppressLint("SetTextI18n")
    fun bind(res: StationSearchRes) {
        itemView.titleTv.text = res.callsign
        itemView.subtitleTv.text = "${res.artist} - ${res.title}"
    }

    fun select(selected: Boolean, isFavorite: Boolean) {
        val bg = itemView.context.color(if (selected) R.color.grey_300 else R.color.grey_50)
        itemView.setBackgroundColor(bg)

        itemView.favoriteBt.visible(selected || isFavorite)
        if (selected || isFavorite) {
            val tint = if (isFavorite) R.color.accentColor else R.color.primaryColor
            itemView.favoriteBt.background.setTintExt(itemView.context.color(tint))
        }
    }
}
