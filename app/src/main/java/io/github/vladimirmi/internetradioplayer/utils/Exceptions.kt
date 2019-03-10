package io.github.vladimirmi.internetradioplayer.utils

import androidx.annotation.StringRes

/**
 * Created by Vladimir Mikhalev 19.09.2018.
 */

class MessageResException(@StringRes val resId: Int) : Exception()

class MessageException(message: String) : Exception(message)