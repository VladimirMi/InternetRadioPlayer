package io.github.vladimirmi.internetradioplayer.presentation.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class SearchStationsAdapter : RecyclerView.Adapter<SearchStationVH>() {

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
        holder.bind(stations[position])
    }

    override fun getItemCount(): Int {
        return stations.size
    }
}

class SearchStationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @SuppressLint("SetTextI18n")
    fun bind(res: StationSearchRes) {
        itemView.titleTv.text = res.callsign
        itemView.subtitleTv.text = "${res.artist} - ${res.title}"
    }
}
