package ru.luckycactus.steamroulette.di.common

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import ru.luckycactus.steamroulette.di.NetworkInterceptors

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugModule {

    companion object {
        @Provides
        @IntoSet
        @NetworkInterceptors
        fun provideStethoInterceptor(): Interceptor = StethoInterceptor()
    }
}