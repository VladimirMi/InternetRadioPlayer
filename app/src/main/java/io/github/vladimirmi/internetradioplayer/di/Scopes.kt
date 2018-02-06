package io.github.vladimirmi.internetradioplayer.di

import android.content.Context
import toothpick.Scope
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

object Scopes {
    const val APP = "app scope"
    const val ROOT_ACTIVITY = "root activity scope"

    val app: Scope get() = Toothpick.openScope(APP)
    val rootActivity: Scope get() = Toothpick.openScopes(APP, ROOT_ACTIVITY)

    //Shortcuts
    val context: Context get() = app.getInstance(Context::class.java)
}