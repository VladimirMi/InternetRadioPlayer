package io.github.vladimirmi.internetradioplayer.extensions

import com.google.android.exoplayer2.util.Util
import java.util.*

/**
 * Created by Vladimir Mikhalev 11.03.2019.
 */
object Formats {

    private val sb = StringBuilder()
    private val formatter = Formatter(sb)

    fun duration(timeMs: Long): String {
        return Util.getStringForTime(sb, formatter, timeMs)
    }

    fun dateTime(timeMs: Long): String {
        sb.setLength(0)
        return formatter.format("%1\$td/%1\$tm/%1\$ty  %1\$tH:%1\$tM:%1\$tS", timeMs).toString()
    }
}