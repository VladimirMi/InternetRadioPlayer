package io.github.vladimirmi.internetradioplayer.extensions

import android.support.annotation.StringRes
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Vladimir Mikhalev 14.11.2017.
 */

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

fun Completable.then(block: () -> Any): Completable {
    return andThen(Completable.fromCallable(block))
}

class ValidationException(@StringRes val resId: Int) : Exception()

