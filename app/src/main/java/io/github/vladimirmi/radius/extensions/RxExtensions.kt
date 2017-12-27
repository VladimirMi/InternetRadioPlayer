package io.github.vladimirmi.radius.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

fun <T : Any> Observable<T>.ioToMain(): Observable<T> {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Single<T>.ioToMain(): Single<T> {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

fun Completable.ioToMain(): Completable {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

