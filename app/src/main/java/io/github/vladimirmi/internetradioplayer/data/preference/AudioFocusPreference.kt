package io.github.vladimirmi.internetradioplayer.data.preference

import android.annotation.SuppressLint
import android.content.SharedPreferences
import io.github.vladimirmi.internetradioplayer.R
import kotlin.reflect.KProperty

/**
 * Created by Vladimir Mikhalev 30.06.2019.
 */

enum class AudioFocusPreference {

    Duck {
        override val nextState get() = Pause
        override val summary = R.string.setting_summary_pause
    },
    Pause {
        override val nextState get() = Duck
        override val summary = R.string.setting_summary_duck
    };

    abstract val nextState: AudioFocusPreference
    abstract val summary: Int

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