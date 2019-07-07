package io.github.vladimirmi.internetradioplayer.data.preference

import android.content.Context
import android.content.SharedPreferences
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.utils.Preference
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class Preferences
@Inject constructor(context: Context) {

    companion object {
        const val PREFERENCES_NAME = "default"
        const val KEY_INITIAL_BUFFER_LENGTH = "INITIAL_BUFFER_LENGTH"
        const val KEY_BUFFER_LENGTH = "BUFFER_LENGTH"
        const val KEY_GLOBAL_PRESET = "GLOBAL_PRESET"
        const val KEY_MAIN_PAGE_ID = "MAIN_PAGE_ID"
        const val KEY_FAVORITE_PAGE_ID = "FAVORITE_PAGE_ID"
        const val KEY_SELECTED_MEDIA_ID = "SELECTED_MEDIA_ID"
        const val KEY_EQUALIZER_ENABLED = "EQUALIZER_ENABLED"
        const val KEY_SEARCH_SCREEN_PATH = "SEARCH_SCREEN_PATH"
        const val KEY_COVER_ART_ENABLED = "COVER_ART_ENABLED"
        const val KEY_AUDIO_FOCUS = "AUDIO_FOCUS"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    var initialBufferLength: Int by Preference(sharedPreferences, KEY_INITIAL_BUFFER_LENGTH, 3)
    var bufferLength: Int by Preference(sharedPreferences, KEY_BUFFER_LENGTH, 6)
    var globalPreset: String by Preference(sharedPreferences, KEY_GLOBAL_PRESET, "")
    var mainPageId: Int by Preference(sharedPreferences, KEY_MAIN_PAGE_ID, R.id.nav_search)
    var favoritePageId: Int by Preference(sharedPreferences, KEY_FAVORITE_PAGE_ID, -1)
    var mediaId: String by Preference(sharedPreferences, KEY_SELECTED_MEDIA_ID, "")
    var equalizerEnabled: Boolean by Preference(sharedPreferences, KEY_EQUALIZER_ENABLED, false)
    var searchScreenPath: String by Preference(sharedPreferences, KEY_SEARCH_SCREEN_PATH, "")
    var coverArtEnabled: Boolean by Preference(sharedPreferences, KEY_COVER_ART_ENABLED, true)
    var audioFocus: AudioFocusPreference by AudioFocusPreferenceDelegate(sharedPreferences)

    private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    private val prefsMap: Map<String, () -> Any> = mapOf(
            KEY_INITIAL_BUFFER_LENGTH to { initialBufferLength },
            KEY_BUFFER_LENGTH to { bufferLength },
            KEY_GLOBAL_PRESET to { globalPreset },
            KEY_MAIN_PAGE_ID to { mainPageId },
            KEY_FAVORITE_PAGE_ID to { favoritePageId },
            KEY_SELECTED_MEDIA_ID to { mediaId },
            KEY_EQUALIZER_ENABLED to { equalizerEnabled },
            KEY_SEARCH_SCREEN_PATH to { searchScreenPath },
            KEY_COVER_ART_ENABLED to { coverArtEnabled },
            KEY_AUDIO_FOCUS to { audioFocus }
    )

    @Suppress("ThrowableNotThrown", "UNCHECKED_CAST")
    fun <T> observe(key: String): Observable<T> {
        return Observable.create { emitter ->
            val value = prefsMap[key]
            if (value == null) emitter.tryOnError(IllegalStateException("Cannot find Key='$key' in the prefsMap"))

            (value?.invoke() as? T)?.let { if (!emitter.isDisposed) emitter.onNext(it) }
                    ?: emitter.tryOnError(IllegalStateException("Preference(key='$key', value='${value?.invoke()}') cannot be casted"))

            val listener: SharedPreferences.OnSharedPreferenceChangeListener =
                    SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
                        if (k == key && !emitter.isDisposed) emitter.onNext(value?.invoke() as T)
                    }
            listeners.add(listener)
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            emitter.setCancellable {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                listeners.remove(listener)
            }
        }
    }
}
