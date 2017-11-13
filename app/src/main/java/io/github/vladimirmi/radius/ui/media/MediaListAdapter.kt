package io.github.vladimirmi.radius.ui.media

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.getIconTextColors
import io.github.vladimirmi.radius.extensions.setBackgroundColorExt
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

class MediaListAdapter(private val callback: MediaItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val GROUP_TITLE = 0
        const val GROUP_ITEM = 1
    }

    private lateinit var stationList: GroupedList<Station>
    private var selected: Station? = null
    private var playing = false

    fun setData(data: GroupedList<Station>) {
        stationList = data
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
            if (stationList.isGroupTitle(position)) GROUP_TITLE else GROUP_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GROUP_TITLE -> MediaGroupTitleVH(inflater.inflate(R.layout.item_group_title, parent, false))
            GROUP_ITEM -> MediaGroupItemVH(inflater.inflate(R.layout.item_group_item, parent, false))
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MediaGroupTitleVH -> holder.bind(stationList.getGroupTitle(position), callback)
            is MediaGroupItemVH -> {
                val media = stationList.getGroupItem(position)
                holder.setCallback(callback, media)
                holder.bind(media)
                if (media.uri == selected?.uri) holder.select(playing)
                else holder.unselect()
            }
        }
    }

    override fun getItemCount(): Int = stationList.groupedSize()

    fun select(station: Station, playing: Boolean) {
        selected = station
        this.playing = playing
        notifyDataSetChanged()
    }
}

class MediaGroupTitleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String, callback: MediaItemCallback) {
        itemView.title.text = title
        itemView.setOnClickListener { callback.onGroupSelected(title) }
    }
}

class MediaGroupItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(station: Station) {
        itemView.name.text = station.title
        itemView.favorite.visibility = if (station.fav) View.VISIBLE else View.INVISIBLE

        val colors = itemView.context.getIconTextColors(station.title[0])
        itemView.icon.text = station.title.first().toString()
        itemView.icon.setTextColor(colors.first)
        itemView.icon.setBackgroundColor(colors.second)
    }

    fun setCallback(callback: MediaItemCallback, station: Station) {
        itemView.setOnClickListener { callback.onItemSelected(station) }
    }

    fun select(playing: Boolean) {
        if (playing) itemView.setBackgroundColorExt(R.color.green_100)
        else itemView.setBackgroundColorExt(R.color.grey_300)
    }

    fun unselect() {
        itemView.setBackgroundColorExt(R.color.grey_50)
    }
}

interface MediaItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(group: String)
}