package ru.luckycactus.steamroulette.di

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.net.SteamApiService
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val STEAM_WEB_API_URL = "https://api.steampowered.com/"

    val steamApiService: SteamApiService by lazy {
        val interceptors = listOf<Interceptor>(
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
            //.addCallAdapterFactory(CoroutineCallAdapterFactory()) //todo remove when migrate tp 2.6.0
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
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
}