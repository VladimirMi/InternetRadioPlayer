package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.02.2019.
 */

class RecordsAdapter : RecyclerView.Adapter<RecordVH>() {

    var data: List<Record> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordVH(view)
    }

    override fun onBindViewHolder(holder: RecordVH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class RecordVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTv = itemView.titleTv

    fun bind(record: Record) {
        titleTv.text = record.name
    }
}