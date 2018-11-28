package io.github.vladimirmi.internetradioplayer.extensions

import android.widget.Toast
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 14.11.2017.
 */

val errorHandler: (Throwable) -> Unit = {
    if (it is MessageResException) {
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
