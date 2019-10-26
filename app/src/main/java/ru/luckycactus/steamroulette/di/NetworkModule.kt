package ru.luckycactus.steamroulette.di

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.utils.MyHttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val STEAM_WEB_API_URL = "https://api.steampowered.com/"

    val steamApiService: SteamApiService by lazy {
        val interceptors = listOf(
            getAuthInterceptor(),
            getLogInterceptor()
        )
        val okHttpClient = getOkHttpClient(interceptors)
        val retrofit = getRetrofit(okHttpClient)

        retrofit.create(SteamApiService::class.java)
    }

    private fun getRetrofit(okHttpClient: OkHttpClient) =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(STEAM_WEB_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private fun getOkHttpClient(interceptors: List<Interceptor>): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)

        interceptors.forEach { builder.addInterceptor(it) }
        return builder.build()
    }

    private fun getAuthInterceptor() = Interceptor { chain ->
        val newUrl = chain.request().url()
            .newBuilder()
            .addQueryParameter("key", BuildConfig.STEAM_WEB_API_KEY)
            .build()

        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        chain.proceed(newRequest)
    }

    private fun getLogInterceptor() =
        MyHttpLoggingInterceptor(setOf("IPlayerService/GetOwnedGames/")).apply {
            level = if (BuildConfig.DEBUG)
                MyHttpLoggingInterceptor.Level.BODY
            else
                MyHttpLoggingInterceptor.Level.NONE
        }
}