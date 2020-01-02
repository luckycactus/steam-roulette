package ru.luckycactus.steamroulette.data.net

import okhttp3.Interceptor
import okhttp3.Response
import ru.luckycactus.steamroulette.BuildConfig
import javax.inject.Inject

class AuthInterceptor @Inject constructor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newUrl = chain.request().url()
            .newBuilder()
            .addQueryParameter("key", BuildConfig.STEAM_WEB_API_KEY)
            .build()

        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}