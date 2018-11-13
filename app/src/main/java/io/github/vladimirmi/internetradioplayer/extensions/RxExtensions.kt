package io.github.vladimirmi.internetradioplayer.extensions

import android.widget.Toast
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

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

val errorHandler: (Throwable) -> Unit = {
    if (it is MessageException) {
        runOnUiThread { Toast.makeText(Scopes.context, it.resId, Toast.LENGTH_SHORT).show() }
    } else {
        Timber.e(it)
        runOnUiThread { Toast.makeText(Scopes.context, it.message, Toast.LENGTH_SHORT).show() }
    }
}

fun <T : Any> Single<T>.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onSuccess: (T) -> Unit = {}
): Disposable = subscribe(onSuccess, onError)

fun Completable.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onComplete: () -> Unit = {}
): Disposable = subscribe(onComplete, onError)

fun <T : Any> Observable<T>.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onNext: (T) -> Unit = {},
        onComplete: () -> Unit = {}
): Disposable = subscribe(onNext, onError, onComplete)
