package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Vladimir Mikhalev 17.11.2017.
 */

abstract class ItemSwipeCallback : ItemTouchHelper.SimpleCallback(0, 0) {

    abstract override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 target: RecyclerView.ViewHolder): Boolean

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            onStartDrag(viewHolder.adapterPosition)

        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            onIdle()
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    abstract fun onStartDrag(position: Int)

    abstract fun onIdle()

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return 0
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}
