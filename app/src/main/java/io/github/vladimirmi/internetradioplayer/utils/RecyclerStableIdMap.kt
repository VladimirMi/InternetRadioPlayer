package io.github.vladimirmi.internetradioplayer.utils

import androidx.recyclerview.widget.RecyclerView
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by Vladimir Mikhalev 18.03.2019.
 */

class RecyclerStableIdMap<T> {

    private val idGenerator = AtomicLong(Long.MIN_VALUE)

    private val map = HashMap<T, Long>()

    fun update(set: Set<T>) {
        for (key in set) {
            if (key !in map) {
                map[key] = idGenerator.getAndIncrement()
            }
        }
        map.keys.removeAll { it !in set }
    }

    fun getId(key: T): Long {
        return map[key] ?: RecyclerView.NO_ID
    }
}