package io.github.vladimirmi.internetradioplayer.data.utils

import android.content.Context
import android.content.SharedPreferences
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.utils.Preference
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
    }

    val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    var initialBufferLength: Int by Preference(sharedPreferences, KEY_INITIAL_BUFFER_LENGTH, 3)
    var bufferLength: Int by Preference(sharedPreferences, KEY_BUFFER_LENGTH, 6)
    var globalPreset: String by Preference(sharedPreferences, KEY_GLOBAL_PRESET, "")
    var mainPageId: Int by Preference(sharedPreferences, KEY_MAIN_PAGE_ID, R.id.nav_search)
    var favoritePageId: Int by Preference(sharedPreferences, KEY_FAVORITE_PAGE_ID, 0)
    var mediaId: String by Preference(sharedPreferences, KEY_SELECTED_MEDIA_ID, "")

}
