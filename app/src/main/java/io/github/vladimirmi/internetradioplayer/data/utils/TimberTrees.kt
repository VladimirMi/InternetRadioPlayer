package io.github.vladimirmi.internetradioplayer.data.utils

import android.content.Context
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet


/**
 * Created by Vladimir Mikhalev 21.11.2017.
 */

class FileLoggingTree
private constructor(context: Context,
                    private val logs: MutableSet<Logs>) : Timber.DebugTree() {

    private val logsDir by lazy {
        val dir = File(context.getExternalFilesDir(null), "Logs")
        dir.mkdir()
        dir
    }
    private val fileNameFormat = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault())
    private val timeStampFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ss:SSS", Locale.getDefault())

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        writeLog(priority, "$tag: $message")
        super.log(priority, tag, message, t)
    }

    private fun writeLog(priority: Int, message: String) {
        val log = Logs.priority(priority)
        if (!logs.contains(log)) return
        @Suppress("TooGenericExceptionCaught")
        try {
            val fileName = fileNameFormat.format(Date()) + ".html"
            val timeStamp = timeStampFormat.format(Date())
            File(logsDir, fileName).appendText(log.format(timeStamp, message))

        } catch (e: Exception) {
            //ignore
        }
    }

    enum class Logs(private val color: String) {
        VERBOSE("white"),
        DEBUG("palegreen"),
        INFO("skyblue"),
        WARN("yellow"),
        ERROR("salmon"),
        ASSERT("orange");

        fun format(timeStamp: String, message: String): String =
                "<p style=\"background:lightgray;\"><strong style=\"background:$color;\">" +
                        "&nbsp&nbsp$timeStamp :&nbsp&nbsp</strong>&nbsp&nbsp$message</p>"

        companion object {
            private val map: Map<Int, Logs> = values().associateBy { it.ordinal + 2 }
            fun priority(int: Int): Logs = map[int] ?: ERROR
        }
    }

    class Builder(private val context: Context) {
        private val logs: MutableSet<Logs> = HashSet()

        fun log(vararg logs: Logs): Builder {
            this.logs.addAll(logs)
            return this
        }

        fun build(): FileLoggingTree {
            return FileLoggingTree(context, logs)
        }
    }
}
