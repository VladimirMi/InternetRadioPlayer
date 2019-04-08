package io.github.vladimirmi.internetradioplayer.presentation.navigation

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import kotlinx.android.synthetic.main.item_drawer.view.*

/**
 * Created by Vladimir Mikhalev 08.04.2019.
 */

class DrawerAdapter(private val menu: Menu) : RecyclerView.Adapter<DrawerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drawer, parent, false)
        return DrawerVH(view)
    }

    override fun onBindViewHolder(holder: DrawerVH, position: Int) {
        holder.titleTv.text = menu.getItem(position).title
    }

    override fun getItemCount(): Int {
        return menu.size()
    }

    fun getItem(position: Int): MenuItem {
        return menu.getItem(position)
    }
}

class DrawerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleTv: TextView = itemView.titleTv
}