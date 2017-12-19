package io.github.vladimirmi.radius.extensions

import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*

/**
 * Created by Vladimir Mikhalev 14.11.2017.
 */

fun <T : Any> (() -> T?).toMaybe(): Maybe<T> = Maybe.create { s ->
    val result = this.invoke()
    if (result != null) s.onSuccess(result)
    s.onComplete()
}

fun <T : Any> (() -> T?).toSingle(): Single<T> = Single.create { s ->
    val result = this.invoke()
    result?.let { s.onSuccess(it) } ?: s.onError(NoSuchElementException())
}

