package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations.PAYLOAD_BACKGROUND_CHANGE
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations.PAYLOAD_SELECTED_CHANGE
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations.defaultOutline
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations.fixedOutline
import kotlinx.android.synthetic.main.item_record.view.*

/**
 * Created by Vladimir Mikhalev 15.02.2019.
 */

class RecordsAdapter : RecyclerView.Adapter<RecordVH>() {

    var onItemClickListener: ((Record) -> Unit)? = null
    var longClickedItem: Record? = null

    private var selectedId: String? = null

    var records: List<Record> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size

                override fun getNewListSize() = value.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].id == value[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition] == value[newItemPosition]
                }
            })
            field = value
            diff.dispatchUpdatesTo(this)

            notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND_CHANGE)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordVH(view)
    }

    override fun onBindViewHolder(holder: RecordVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) holder.select(records[position].id == selectedId)
        if (payloads.contains(PAYLOAD_BACKGROUND_CHANGE)) holder.setBackground(position, itemCount)
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: RecordVH, position: Int) {
        val record = records[position]
        holder.bind(record)
        holder.select(record.id == selectedId)
        holder.setBackground(position, itemCount)
        holder.itemView.setOnClickListener { onItemClickListener?.invoke(record) }
        holder.itemView.setOnLongClickListener { longClickedItem = record; false }
    }

    override fun getItemCount(): Int {
        return records.size
    }

    fun selectRecord(id: String): Int {
        val oldPos = records.indexOfFirst { it.id == selectedId }
        val newPos = records.indexOfFirst { it.id == id }
        selectedId = id
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
        return newPos
    }
}


class RecordVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

    private val titleTv = itemView.titleTv
    private val createdAtTv = itemView.createdAtTv

    private var bgColor: Int = itemView.context.color(R.color.grey_50)

    init {
        itemView.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(R.id.context_menu_records, R.id.context_menu_action_delete, 0, R.string.menu_delete)
    }

    fun bind(record: Record) {
        titleTv.text = record.name
        createdAtTv.text = record.createdAtString
    }

    fun select(selected: Boolean) {
        bgColor = itemView.context.color(if (selected) R.color.accent_light else R.color.grey_50)
        (itemView.background as? GradientDrawable)?.setColor(bgColor)
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
        itemView.background = ContextCompat.getDrawable(itemView.context, bg)
        (itemView.background as? GradientDrawable)?.setColor(bgColor)

        if (Build.VERSION.SDK_INT < 21) return
        itemView.outlineProvider = if (middle) fixedOutline else defaultOutline
    }
}