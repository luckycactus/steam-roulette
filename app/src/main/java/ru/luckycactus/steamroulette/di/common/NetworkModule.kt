package ru.luckycactus.steamroulette.di.common

import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.net.interceptors.AuthInterceptor
import ru.luckycactus.steamroulette.data.net.interceptors.MyHttpLoggingInterceptor
import ru.luckycactus.steamroulette.data.net.services.SteamApiService
import ru.luckycactus.steamroulette.data.net.services.SteamStoreApiService
import ru.luckycactus.steamroulette.data.utils.enableTls12OnOldApis
import ru.luckycactus.steamroulette.di.InterceptorSet
import ru.luckycactus.steamroulette.di.NetworkInterceptorSet
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
abstract class NetworkModule {

    @Multibinds
    @NetworkInterceptorSet
    abstract fun networkInterceptorSet(): Set<Interceptor>

    @IntoSet
    @InterceptorSet
    @Binds
    abstract fun provideAuthInterceptor(authInterceptor: AuthInterceptor): Interceptor

    companion object {
        @Singleton
        @Provides
        fun provideSteamApiService(@Named("steam-api") retrofit: Retrofit): SteamApiService =
            retrofit.create(SteamApiService::class.java)

        @Singleton
        @Provides
        fun provideSteamStoreApiService(@Named("steam-store-api") retrofit: Retrofit): SteamStoreApiService =
            retrofit.create(SteamStoreApiService::class.java)

        @JvmStatic
        @Provides
        @Named("steam-api")
        fun provideRetrofitForSteamApi(
            okHttpClient: OkHttpClient,
            @Named("api") moshi: Moshi
        ): Retrofit =
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://api.steampowered.com/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        @JvmStatic
        @Provides
        @Named("steam-store-api")
        fun provideRetrofitForSteamStoreApi(
            okHttpClient: OkHttpClient,
            @Named("api") moshi: Moshi
        ): Retrofit {
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://store.steampowered.com/api/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }

        @JvmStatic
        @Provides
        @Singleton
        fun provideOkHttpClient(
            @InterceptorSet interceptors: Set<@JvmSuppressWildcards Interceptor>,
            @NetworkInterceptorSet networkInterceptors: Set<@JvmSuppressWildcards Interceptor>
        ): OkHttpClient {
            return OkHttpClient.Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
                interceptors.forEach { addInterceptor(it) }
                networkInterceptors.forEach { addNetworkInterceptor(it) }
                enableTls12OnOldApis(this)
            }.build()
        }

        @JvmStatic
        @IntoSet
        @InterceptorSet
        @Provides
        fun provideLogInterceptor(): Interceptor =
            MyHttpLoggingInterceptor(
                setOf("IPlayerService/GetOwnedGames/")
            ).apply {
                level = if (BuildConfig.DEBUG)
                    MyHttpLoggingInterceptor.Level.BODY
                else
                    MyHttpLoggingInterceptor.Level.NONE
            }
    }
}