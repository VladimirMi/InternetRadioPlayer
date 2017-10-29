package io.github.vladimirmi.radius.ui.media

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.setBackgroundColorExt
import io.github.vladimirmi.radius.model.entity.GroupingMedia
import io.github.vladimirmi.radius.model.entity.Media
import kotlinx.android.synthetic.main.item_group_item.view.*
import kotlinx.android.synthetic.main.item_group_title.view.*

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

class MediaListAdapter(private val itemCallback: MediaItemCallback)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val GROUP_TITLE = 0
        const val GROUP_ITEM = 1
    }

    private val mediaList = GroupingMedia()

    fun setData(data: List<Media>) {
        mediaList.setData(data)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
            if (mediaList.isGroupTitle(position)) GROUP_TITLE else GROUP_ITEM

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
            is MediaGroupTitleVH -> holder.bind(mediaList.getGroupTitle(position))
            is MediaGroupItemVH -> holder.bind(mediaList.getGroupItem(position), itemCallback)
        }
    }

    override fun getItemCount(): Int = mediaList.size()

    fun getItemPosition(media: Media): Int {
        return mediaList.getGroupItemPosition(media)
    }
}

class MediaGroupTitleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String) {
        itemView.title.text = title
    }
}

class MediaGroupItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(media: Media, itemCallback: MediaItemCallback) {
        itemView.name.text = media.name
        itemView.setOnClickListener { itemCallback.onItemSelected(media) }
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
    fun onItemSelected(media: Media)
}