package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import androidx.recyclerview.widget.DiffUtil
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList

/**
 * Created by Vladimir Mikhalev 29.11.2018.
 */

class FavoriteListDiff(private val oldList: FlatStationsList,
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
                if (newList.isGroup(newItemPosition) && oldList.isGroup(oldItemPosition)) {
                    if (newList.getGroup(newItemPosition).expanded != oldList.getGroup(oldItemPosition).expanded) {
                        return false
                    }
                } else if (newList.isStation(newItemPosition) && oldList.isStation(oldItemPosition)) {
                    if (newList.getStation(newItemPosition).name != oldList.getStation(oldItemPosition).name) {
                        return false
                    }
                }
                return true
            }
        })
    }
}