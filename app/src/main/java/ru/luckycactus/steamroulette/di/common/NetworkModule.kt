package ru.luckycactus.steamroulette.di.common

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.utils.MyHttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
abstract class NetworkModule {

    @Module
    companion object {

        @JvmStatic
        @Singleton
        @Provides
        fun provideSteamApiService(retrofit: Retrofit): SteamApiService =
            retrofit.create(SteamApiService::class.java)

        @JvmStatic
        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient) =
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://api.steampowered.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        @JvmStatic
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)

            //todo multibindings
            listOf(
                getAuthInterceptor(),
                getLogInterceptor()
            ).forEach { builder.addInterceptor(it) }
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
}