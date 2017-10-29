package io.github.vladimirmi.radius.model.entity

import android.net.Uri

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

data class Media(val name: String,
                 val uri: Uri,
                 val fav: Boolean = false,
                 val genres: ArrayList<String> = ArrayList())