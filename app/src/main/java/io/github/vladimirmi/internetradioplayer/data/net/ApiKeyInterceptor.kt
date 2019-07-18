package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by Vladimir Mikhalev 31.03.2019.
 */

class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        return if (originalRequest.url().host() == UberStationsService.HOST) {
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