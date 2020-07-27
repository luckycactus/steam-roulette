package ru.luckycactus.steamroulette.di.common

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import ru.luckycactus.steamroulette.di.NetworkInterceptorSet

@Module
@InstallIn(ApplicationComponent::class)
abstract class DebugModule {

    companion object {
        @Provides
        @IntoSet
        @NetworkInterceptorSet
        fun provideStethoInterceptor(): Interceptor = StethoInterceptor()
    }
}