package io.github.vladimirmi.internetradioplayer.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import kotlin.reflect.KProperty

/**
 * Created by Vladimir Mikhalev 07.10.2017.
 */

class Preference<T>(
        private val prefs: SharedPreferences,
        private val name: String,
        private val default: T) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
            findPreference(name, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> findPreference(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            is Set<*> -> getStringSet(name, default as Set<String>)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }
        res as T
    }

    @SuppressLint("CommitPrefEdits")
    @Suppress("UNCHECKED_CAST")
    private fun putPreference(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            is Set<*> -> putStringSet(name, value as Set<String>)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }.apply()
    }
}
