package io.github.vladimirmi.internetradioplayer.presentation.data

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.model.Data
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.themeAttrData
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.defaultOutline
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.fixedOutline
import kotlinx.android.synthetic.main.item_station.view.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

private const val PAYLOAD_SELECTED_CHANGE = "PAYLOAD_SELECTED_CHANGE"

class DataAdapter : RecyclerView.Adapter<DataVH>() {

    private var selectedDataUri: String? = null

    var onItemClickListener: ((Data) -> Unit)? = null

    var onAddToFavListener: ((Data) -> Unit)? = null

    var data: List<Data> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_station, parent, false)
        return DataVH(view)
    }

    override fun onBindViewHolder(holder: DataVH, position: Int, payloads: MutableList<Any>) {
        when {
            payloads.contains(PAYLOAD_SELECTED_CHANGE) -> holder.select(data[position], selectedDataUri)
            else -> super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: DataVH, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.setBackground(position, itemCount)
        holder.select(item, selectedDataUri)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(item) }
        holder.itemView.favoriteBt?.setOnClickListener { onAddToFavListener?.invoke(item) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun selectData(uri: String): Int {
        val oldPos = data.indexOfFirst { it.uri == selectedDataUri }
        val newPos = data.indexOfFirst { it.uri == uri }
        selectedDataUri = uri
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
        return newPos
    }
}

class DataVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleTv = itemView.titleTv
    private val subtitleTv = itemView.subtitleTv
    private val favoriteBt = itemView.favoriteBt

    fun bind(data: Data) {
        titleTv.text = data.title
        subtitleTv.text = data.subtitle
    }

    fun select(data: Data, selectedUri: String?) {
        val selected = data.uri == selectedUri
        val bg = if (selected) itemView.context.themeAttrData(R.attr.colorSecondaryVariant)
        else itemView.context.themeAttrData(R.attr.colorSurface)

        (itemView.background as? GradientDrawable)?.setColor(bg)

        favoriteBt.visible(selected || data.isFavorite)
        if (selected || data.isFavorite) {
            val tint = if (data.isFavorite) R.color.orange_500 else R.color.primary_variant
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
        itemView.setBackgroundResource(bg)

        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
    }
}
