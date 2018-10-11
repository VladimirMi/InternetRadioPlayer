package io.github.vladimirmi.internetradioplayer.data.utils

import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by Vladimir Mikhalev 11.10.2018.
 */

private const val TRIES = 4

class ExponentialBackoff {

    private var counter = 0
    private var tryTask: TimerTask? = null

    fun schedule(onTry: () -> Unit): Boolean {
        tryTask?.cancel()

        if (counter == TRIES) {
            counter = 0
            tryTask = null
            return false
        }

        tryTask = Timer().schedule(calcBackoff(counter)) {
            counter++
            onTry.invoke()
        }
        return true
    }

    private fun calcBackoff(n: Int): Long {
        return (Math.pow(2.0, n.toDouble()) * 1000).toLong()
    }

}
