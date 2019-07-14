package io.github.vladimirmi.internetradioplayer.data.net

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.github.vladimirmi.internetradioplayer.data.utils.DiskCacheManager
import io.reactivex.schedulers.Schedulers
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

    private const val CONNECT_TIMEOUT = 10000L
    private const val READ_TIMEOUT = 10000L
    private const val WRITE_TIMEOUT = 10000L


    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()

    fun cachedOkHttpClient(cacheManager: DiskCacheManager): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(DiskCacheInterceptor(cacheManager))
            .addNetworkInterceptor(ApiKeyInterceptor())
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()

    private val gson: Gson = GsonBuilder().create()
    private val converterFactory: GsonConverterFactory = GsonConverterFactory.create(gson)

    fun getUberStationsService(client: OkHttpClient): UberStationsService {
        return setupRetrofit(client, converterFactory)
                .baseUrl(UberStationsService.BASE_URL)
                .build()
                .create(UberStationsService::class.java)
    }

    fun getCoverArtService(client: OkHttpClient): CoverArtService {
        return setupRetrofit(client, converterFactory)
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
}
