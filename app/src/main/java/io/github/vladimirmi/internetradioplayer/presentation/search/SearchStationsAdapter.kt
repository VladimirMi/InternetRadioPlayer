package io.github.vladimirmi.internetradioplayer.presentation.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.visible
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class SearchStationsAdapter : RecyclerView.Adapter<SearchStationVH>() {

    private var selectedStation: StationSearchRes? = null

    var onItemClickListener: ((StationSearchRes) -> Unit)? = null
    var onAddToFavListener: ((StationSearchRes) -> Unit)? = null

    var stations: List<StationSearchRes> = ArrayList()
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
        holder.bind(station)
        holder.select(station.id == selectedStation?.id)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(station) }
        holder.itemView.addToFav?.setOnClickListener { onAddToFavListener?.invoke(station) }
    }

    override fun getItemCount(): Int {
        return stations.size
    }

    fun selectStation(station: StationSearchRes) {
        val oldPos = stations.indexOf(selectedStation)
        val newPos = stations.indexOf(station)
        selectedStation = station
        notifyItemChanged(oldPos)
        notifyItemChanged(newPos)
    }
}

class SearchStationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @SuppressLint("SetTextI18n")
    fun bind(res: StationSearchRes) {
        itemView.titleTv.text = res.callsign
        itemView.subtitleTv.text = "${res.artist} - ${res.title}"
    }

    fun select(selected: Boolean) {
        val bg = itemView.context.color(if (selected) R.color.grey_300 else R.color.grey_50)
        itemView.setBackgroundColor(bg)
        itemView.addToFav.visible(selected)
    }
}
