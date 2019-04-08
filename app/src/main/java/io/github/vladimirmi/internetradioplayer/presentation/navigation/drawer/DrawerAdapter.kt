package io.github.vladimirmi.internetradioplayer.presentation.navigation.drawer

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.themeAttrData
import io.github.vladimirmi.internetradioplayer.presentation.favorite.stations.PAYLOAD_SELECTED_CHANGE
import kotlinx.android.synthetic.main.item_drawer.view.*

/**
 * Created by Vladimir Mikhalev 08.04.2019.
 */

class DrawerAdapter(private val menu: Menu) : RecyclerView.Adapter<DrawerVH>() {

    var onItemSelectedListener: ((MenuItem) -> Unit)? = null
    private var selectedId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drawer, parent, false)
        return DrawerVH(view)
    }

    override fun onBindViewHolder(holder: DrawerVH, position: Int, payloads: MutableList<Any>) {
        val item = menu.getItem(position)
        if (payloads.contains(PAYLOAD_SELECTED_CHANGE)) holder.select(item.itemId == selectedId)
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)

    }

    override fun onBindViewHolder(holder: DrawerVH, position: Int) {
        val item = menu.getItem(position)
        holder.titleTv.text = item.title
        holder.select(item.itemId == selectedId)
        holder.itemView.setOnClickListener { onItemSelectedListener?.invoke(item) }
    }

    override fun getItemCount(): Int {
        return menu.size()
    }

    fun getItem(position: Int): MenuItem {
        return menu.getItem(position)
    }

    fun selectItem(id: Int) {
        val oldPos = menu.indexOfFirst { it.itemId == selectedId }
        val newPos = menu.indexOfFirst { it.itemId == id }
        selectedId = id
        notifyItemChanged(oldPos, PAYLOAD_SELECTED_CHANGE)
        notifyItemChanged(newPos, PAYLOAD_SELECTED_CHANGE)
    }

    private fun Menu.indexOfFirst(bloc: (MenuItem) -> Boolean): Int {
        (0 until size()).forEach {
            val item = getItem(it)
            if (bloc.invoke(item)) return it
        }
        return -1
    }
}

class DrawerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val titleTv: TextView = itemView.titleTv

    fun select(isSelected: Boolean) {
        val bgColor = itemView.context.color(if (isSelected) R.color.grey_300 else R.color.grey_50)
        val textColor = itemView.context.themeAttrData(if (isSelected) R.attr.colorPrimary else R.attr.colorOnSurface)
        itemView.setBackgroundColor(bgColor)
        titleTv.setTextColor(textColor)
    }
}