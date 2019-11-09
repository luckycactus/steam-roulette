package ru.luckycactus.steamroulette.di.common

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.utils.MyHttpLoggingInterceptor
import ru.luckycactus.steamroulette.di.qualifier.InterceptorSet
import ru.luckycactus.steamroulette.di.qualifier.NetworkInterceptorSet
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
abstract class NetworkModule {

    @Multibinds
    @NetworkInterceptorSet
    internal abstract fun networkInterceptorSet(): Set<Interceptor>

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
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://api.steampowered.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        @JvmStatic
        @Provides
        @Singleton
        fun provideOkHttpClient(
            @InterceptorSet interceptors: Set<@JvmSuppressWildcards Interceptor>,
            @NetworkInterceptorSet networkInterceptors: Set<@JvmSuppressWildcards Interceptor>
        ): OkHttpClient =
            OkHttpClient.Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
                interceptors.forEach { addInterceptor(it) }
                networkInterceptors.forEach { addNetworkInterceptor(it) }
            }.build()

        //todo Вынести в отдельный класс
        @JvmStatic
        @IntoSet
        @InterceptorSet
        @Provides
        fun provideAuthInterceptor(): Interceptor = Interceptor { chain ->
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

        @JvmStatic
        @IntoSet
        @InterceptorSet
        @Provides
        fun provideLogInterceptor(): Interceptor =
            MyHttpLoggingInterceptor(setOf("IPlayerService/GetOwnedGames/")).apply {
                level = if (BuildConfig.DEBUG)
                    MyHttpLoggingInterceptor.Level.BODY
                else
                    MyHttpLoggingInterceptor.Level.NONE
            }

    }
}