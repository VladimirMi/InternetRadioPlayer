package io.github.vladimirmi.radius.model.manager

import android.content.Context
import io.github.vladimirmi.radius.extensions.Preference
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class Preferences
@Inject constructor(context: Context) {

    var firstRun: Boolean by Preference(context, "first_run", true)
}