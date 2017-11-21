package io.github.vladimirmi.radius.extensions

import android.util.Log
import io.github.vladimirmi.radius.model.manager.Preferences
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vladimir Mikhalev 21.11.2017.
 */

class FileLoggingTree(prefs: Preferences) : Timber.DebugTree() {

    private val logsDir by lazy {
        val dir = File(prefs.appDirPath, "Logs")
        dir.mkdir()
        dir
    }
    private val fileNameFormat = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault())
    private val timeStampFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ss:SSS", Locale.getDefault())

    override fun e(message: String, vararg args: Any?) {
        log(Log.ERROR, message, args)
    }

    override fun d(message: String, vararg args: Any?) {
        log(Log.DEBUG, message, args)
    }

    override fun i(message: String, vararg args: Any?) {
        log(Log.INFO, message, args)
    }

    override fun w(message: String, vararg args: Any?) {
        log(Log.WARN, message, args)
    }

    override fun log(priority: Int, message: String, vararg args: Any?) {
        val tag = getTag().trim('$')
        Timber.tag(tag)
        writeLog(priority, "$tag: $message")
        super.log(priority, message, *args)
    }

    private fun writeLog(priority: Int, message: String) {
        try {
            val fileName = fileNameFormat.format(Date()) + ".html"
            val timeStamp = timeStampFormat.format(Date())
            File(logsDir, fileName).appendText(Logs.priority(priority).format(timeStamp, message))

        } catch (e: Exception) {
            super.e(e)
        }
    }

    private fun getTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= 5) {
            throw IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?")
        }
        return createStackElementTag(stackTrace[5])
    }

    enum class Logs(private val color: String) {
        VERBOSE("white"),
        DEBUG("palegreen"),
        INFO("skyblue"),
        WARN("yellow"),
        ERROR("salmon"),
        ASSERT("orange");

        fun format(timeStamp: String, message: String): String =
                "<p style=\"background:lightgray;\"><strong style=\"background:$color;\">&nbsp&nbsp$timeStamp :&nbsp&nbsp</strong>&nbsp&nbsp$message</p>"

        companion object {
            private val map = values().associateBy { it.ordinal + 2 }
            fun priority(int: Int) = map[int]!!
        }
    }
}