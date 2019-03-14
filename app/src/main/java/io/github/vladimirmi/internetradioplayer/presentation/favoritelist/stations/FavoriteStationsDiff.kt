package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import androidx.recyclerview.widget.DiffUtil
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList

/**
 * Created by Vladimir Mikhalev 29.11.2018.
 */

class FavoriteStationsDiff(private val oldList: FlatStationsList,
                           private val newList: FlatStationsList) {

    fun calc(): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return newList.getId(newItemPosition) == oldList.getId(oldItemPosition)
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return isStationTheSame(oldItemPosition, newItemPosition)
                        || isGroupTheSame(oldItemPosition, newItemPosition)
            }
        })
    }

    private fun isStationTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList.isStation(oldPos) && newList.isStation(newPos) &&
                oldList.getStation(oldPos) == newList.getStation(newPos)
    }

    private fun isGroupTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList.isGroup(oldPos) && newList.isGroup(newPos) &&
                oldList.getGroup(oldPos) == newList.getGroup(newPos) &&
                oldList.getGroup(oldPos).stations == newList.getGroup(newPos).stations
    }
}