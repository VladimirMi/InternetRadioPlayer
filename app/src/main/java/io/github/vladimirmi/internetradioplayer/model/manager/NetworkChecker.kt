package io.github.vladimirmi.internetradioplayer.model.manager

import android.content.Context
import android.net.ConnectivityManager
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.01.2018.
 */

class NetworkChecker
@Inject constructor(context: Context) {

    private val cm by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    fun isAvailable() = cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected

    fun singleAvailable(): Single<Boolean> = available().filter { it }.firstOrError()

    fun available(): Observable<Boolean> =
            Observable.interval(0, 2, TimeUnit.SECONDS)
                    .map { isAvailable() }
                    .distinctUntilChanged()
}
