package ru.luckycactus.steamroulette.di.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.net.*
import ru.luckycactus.steamroulette.data.utils.enableTls12OnOldApis
import ru.luckycactus.steamroulette.di.qualifier.InterceptorSet
import ru.luckycactus.steamroulette.di.qualifier.NetworkInterceptorSet
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
abstract class NetworkModule {

    @Multibinds
    @NetworkInterceptorSet
    abstract fun networkInterceptorSet(): Set<Interceptor>

    @IntoSet
    @InterceptorSet
    @Binds
    abstract fun provideAuthInterceptor(authInterceptor: AuthInterceptor): Interceptor

    @Module
    companion object {

        @JvmStatic
        @Singleton
        @Provides
        fun provideSteamApiService(@Named("steam-api") retrofit: Retrofit): SteamApiService =
            retrofit.create(SteamApiService::class.java)

        @JvmStatic
        @Singleton
        @Provides
        fun provideSteamStoreApiService(@Named("steam-store-api") retrofit: Retrofit): SteamStoreApiService =
            retrofit.create(SteamStoreApiService::class.java)

        @JvmStatic
        @Provides
        @Named("steam-api")
        fun provideRetrofitForSteamApi(okHttpClient: OkHttpClient, @Named("api") gson: Gson): Retrofit =
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://api.steampowered.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        @JvmStatic
        @Provides
        @Named("steam-store-api")
        fun provideRetrofitForSteamStoreApi(okHttpClient: OkHttpClient, @Named("api") gson: Gson): Retrofit {
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://store.steampowered.com/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
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
            MyHttpLoggingInterceptor(setOf("IPlayerService/GetOwnedGames/"))
                .apply {
                    level = if (BuildConfig.DEBUG)
                        MyHttpLoggingInterceptor.Level.BODY
                    else
                        MyHttpLoggingInterceptor.Level.NONE
                }

        @JvmStatic
        @Provides
        @Named("api")
        fun provideGsonForApi(
            systemRequirementsTypeAdapterFactory: SystemRequirementsTypeAdapterFactory,
            requiredAgeTypeAdapterFactory: RequiredAgeTypeAdapterFactory
        ) = GsonBuilder()
            .registerTypeAdapterFactory(systemRequirementsTypeAdapterFactory)
            .registerTypeAdapterFactory(requiredAgeTypeAdapterFactory)
            .create()

    }
}