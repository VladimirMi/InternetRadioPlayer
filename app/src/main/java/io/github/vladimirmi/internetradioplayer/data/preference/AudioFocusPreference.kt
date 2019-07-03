package io.github.vladimirmi.internetradioplayer.data.preference

import android.annotation.SuppressLint
import android.content.SharedPreferences
import kotlin.reflect.KProperty

/**
 * Created by Vladimir Mikhalev 30.06.2019.
 */

enum class AudioFocusPreference {

    Duck {
        override fun nextState() = Pause
    },
    Pause {
        override fun nextState() = Duck
    };

    abstract fun nextState(): AudioFocusPreference

}

class AudioFocusPreferenceDelegate(private val prefs: SharedPreferences) {

    private val default = AudioFocusPreference.Duck.name

    operator fun getValue(thisRef: Any?, property: KProperty<*>): AudioFocusPreference = getPreference()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: AudioFocusPreference) = putPreference(value)

    private fun getPreference(): AudioFocusPreference {
        val prefName = prefs.getString(Preferences.KEY_AUDIO_FOCUS, default)!!
        return AudioFocusPreference.valueOf(prefName)
    }

    @SuppressLint("CommitPrefEdits")
    private fun putPreference(value: AudioFocusPreference) = with(prefs.edit()) {
        putString(Preferences.KEY_AUDIO_FOCUS, value.name)
    }.apply()
}