package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import androidx.recyclerview.widget.DiffUtil
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 29.11.2018.
 */

class FavoriteListDiff(private val oldList: FlatStationsList,
                       private val newList: FlatStationsList) {

    fun calc(): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                Timber.e("getOldListSize: ${oldList.size}")
                return oldList.size
            }

            override fun getNewListSize(): Int {
                Timber.e("getNewListSize: ${newList.size}")
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val b = newList.getId(newItemPosition) == oldList.getId(oldItemPosition)
                Timber.e("areItemsTheSame: $b")
                return b
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })
    }
}