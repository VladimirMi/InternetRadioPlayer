package io.github.vladimirmi.internetradioplayer.extensions

import android.support.annotation.StringRes

/**
 * Created by Vladimir Mikhalev 19.09.2018.
 */

class ValidationException(@StringRes val resId: Int) : Exception()
