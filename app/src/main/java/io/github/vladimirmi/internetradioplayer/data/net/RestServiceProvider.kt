package io.github.vladimirmi.internetradioplayer.data.net

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

object RestServiceProvider {

    private const val CONNECT_TIMEOUT = 5000L
    private const val READ_TIMEOUT = 5000L
    private const val WRITE_TIMEOUT = 5000L


    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(apiKeyInterceptor())
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()

    val gson: Gson = GsonBuilder().create()

    fun getUberStationsService(): UberStationsService {
        return setupRetrofit(okHttpClient, GsonConverterFactory.create(gson))
                .baseUrl(UberStationsService.BASE_URL)
                .build()
                .create(UberStationsService::class.java)
    }

    fun getCoverArtService(): CoverArtService {
        return setupRetrofit(okHttpClient, GsonConverterFactory.create(gson))
                .baseUrl(CoverArtService.BASE_URL)
                .build()
                .create(CoverArtService::class.java)
    }

    private fun setupRetrofit(okHttp: OkHttpClient, factory: Converter.Factory): Retrofit.Builder {
        return Retrofit.Builder()
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttp)
    }

    private fun apiKeyInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            if (originalRequest.url().host() == UberStationsService.HOST) {
                val url = originalRequest.url().newBuilder()
                        .addQueryParameter("callback", "json")
                        .addQueryParameter("intl", "1")
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
}
