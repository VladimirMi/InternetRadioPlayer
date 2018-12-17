package io.github.vladimirmi.internetradioplayer.data.net

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

private const val HOST = "api.dar.fm"
private const val BASE_URL = "http://$HOST"
private const val CONNECT_TIMEOUT = 5000L
private const val READ_TIMEOUT = 5000L
private const val WRITE_TIMEOUT = 5000L


fun OkHttpClient.getUberStationsService(factory: Converter.Factory): UberStationsService {
    return Retrofit.Builder().createRetrofit(this, factory).create(UberStationsService::class.java)
}

fun OkHttpClient.Builder.createClient(): OkHttpClient {
    return addNetworkInterceptor(apiKeyInterceptor())
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
}

private fun Retrofit.Builder.createRetrofit(okHttp: OkHttpClient, factory: Converter.Factory): Retrofit {
    return baseUrl(BASE_URL)
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttp)
            .build()
}

private fun apiKeyInterceptor(): Interceptor {
    return Interceptor { chain ->
        val originalRequest = chain.request()
        if (originalRequest.url().host() == HOST) {
            val url = originalRequest.url().newBuilder()
                    .addQueryParameter("callback", "json")
                    .addQueryParameter("partner_token", BuildConfig.PARTNER_TOKEN)
                    .build()

            val request = originalRequest.newBuilder()
                    .url(url)
                    .build()
            chain.proceed(request)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
