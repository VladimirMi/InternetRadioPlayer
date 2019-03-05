package io.github.vladimirmi.internetradioplayer.data.utils

import android.content.Context
import android.content.SharedPreferences
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.utils.Preference
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

const val PREFERENCES_NAME = "default"
const val INITIAL_BUFFER_LENGTH_KEY = "INITIAL_BUFFER_LENGTH"
const val BUFFER_LENGTH_KEY = "BUFFER_LENGTH"
const val GLOBAL_PRESET_KEY = "GLOBAL_PRESET"
const val MAIN_PAGE_ID_KEY = "MAIN_PAGE_ID_KEY"
const val FAVORITE_PAGE_ID_KEY = "FAVORITE_PAGE_ID_KEY"

class Preferences
@Inject constructor(context: Context) {

    val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    var initialBufferLength: Int by Preference(sharedPreferences, INITIAL_BUFFER_LENGTH_KEY, 3)
    var bufferLength: Int by Preference(sharedPreferences, BUFFER_LENGTH_KEY, 6)
    var globalPreset: String by Preference(sharedPreferences, GLOBAL_PRESET_KEY, "")
    var mainPageId: Int by Preference(sharedPreferences, MAIN_PAGE_ID_KEY, R.id.nav_search)
    var favoritePageId: Int by Preference(sharedPreferences, FAVORITE_PAGE_ID_KEY, 0)

}
