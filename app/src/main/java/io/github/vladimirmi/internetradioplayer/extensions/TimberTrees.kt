package io.github.vladimirmi.internetradioplayer.extensions

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vladimir Mikhalev 21.11.2017.
 */

class FileLoggingTree(context: Context) : Timber.DebugTree() {

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
        try {
            val fileName = fileNameFormat.format(Date()) + ".html"
            val timeStamp = timeStampFormat.format(Date())
            File(logsDir, fileName).appendText(Logs.priority(priority).format(timeStamp, message))

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
                "<p style=\"background:lightgray;\"><strong style=\"background:$color;\">&nbsp&nbsp$timeStamp :&nbsp&nbsp</strong>&nbsp&nbsp$message</p>"

        companion object {
            private val map = values().associateBy { it.ordinal + 2 }
            fun priority(int: Int) = map[int]!!
        }
    }
}

class CrashlyticsTree : Timber.DebugTree() {
    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority)
        Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag)
        Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message)

        Crashlytics.logException(t ?: Exception(message))

        super.log(priority, tag, message, t)
    }
}