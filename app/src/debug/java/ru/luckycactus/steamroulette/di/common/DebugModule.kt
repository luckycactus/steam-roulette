package ru.luckycactus.steamroulette.di.common

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import ru.luckycactus.steamroulette.di.qualifier.NetworkInterceptorSet

@Module
abstract class DebugModule {

    @Module
    companion object {

        @JvmStatic
        @Provides
        @IntoSet
        @NetworkInterceptorSet
        fun provideStethoInterceptor(): Interceptor = StethoInterceptor()
    }
}