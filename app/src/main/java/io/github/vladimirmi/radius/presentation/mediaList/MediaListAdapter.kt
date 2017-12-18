package io.github.vladimirmi.radius.presentation.mediaList

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.color
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationRepository
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

    private val repository = Scopes.app.getInstance(StationRepository::class.java)
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
            is MediaGroupTitleVH -> {
                val title = stationList.getGroupTitle(position)
                holder.bind(title, callback)
                holder.expanded(stationList.isGroupVisible(title))
                if (!stationList.isGroupVisible(title) && selected?.group == title) {
                    holder.select(playing)
                } else {
                    holder.unselect()
                }
            }
            is MediaGroupItemVH -> {
                val station = stationList.getGroupItem(position)
                holder.bind(station)
                holder.setIcon(repository.getStationIcon(station.title).blockingGet())
                holder.setCallback(callback, station)
                if (station.uri == selected?.uri) {
                    holder.select(playing)
                } else {
                    holder.unselect()
                }
            }
        }
    }

    override fun getItemCount(): Int = stationList.groupedSize()

    fun selectItem(station: Station, playing: Boolean) {
        selected = station
        this.playing = playing
        notifyDataSetChanged()
    }

    fun getStation(position: Int): Station? {
        return if (stationList.isGroupTitle(position)) null
        else stationList.getGroupItem(position)
    }
}

class MediaGroupTitleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String, callback: MediaItemCallback) {
        itemView.title.text = title
        itemView.setOnClickListener { callback.onGroupSelected(title) }
    }

    fun expanded(expanded: Boolean) {
        itemView.ic_expanded.setImageResource(if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand)
    }

    fun select(playing: Boolean) {
        if (playing) itemView.setBackgroundColor(itemView.context.color(R.color.green_100))
        else itemView.setBackgroundColor(itemView.context.color(R.color.grey_300))
    }

    fun unselect() {
        itemView.setBackgroundColor(itemView.context.color(R.color.grey_50))
    }
}

class MediaGroupItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(station: Station) {
        with(itemView) {
            name.text = station.title
            favorite.visibility = if (station.favorite) View.VISIBLE else View.INVISIBLE
        }
    }

    fun setIcon(bitmap: Bitmap) {
        itemView.iconIv.setImageBitmap(bitmap)
    }

    fun setCallback(callback: MediaItemCallback, station: Station) {
        itemView.setOnClickListener { callback.onItemSelected(station) }
    }

    fun select(playing: Boolean) {
        if (playing) itemView.setBackgroundColor(itemView.context.color(R.color.green_100))
        else itemView.setBackgroundColor(itemView.context.color(R.color.grey_300))
    }

    fun unselect() {
        itemView.setBackgroundColor(itemView.context.color(R.color.grey_50))
    }
}

interface MediaItemCallback {
    fun onItemSelected(station: Station)
    fun onGroupSelected(group: String)
}