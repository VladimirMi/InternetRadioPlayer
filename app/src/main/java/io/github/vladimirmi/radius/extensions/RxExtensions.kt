package io.github.vladimirmi.radius.extensions

import io.reactivex.Maybe

/**
 * Created by Vladimir Mikhalev 14.11.2017.
 */

fun <T : Any> (() -> T?).toMaybe(): Maybe<T> = Maybe.create { s ->
    val result = this()
    if (result != null) s.onSuccess(result); s.onComplete()
}