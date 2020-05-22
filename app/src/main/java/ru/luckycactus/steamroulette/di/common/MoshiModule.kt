package ru.luckycactus.steamroulette.di.common

import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import okhttp3.Interceptor
import ru.luckycactus.steamroulette.data.net.adapters.RequiredAgeMoshiAdapter
import ru.luckycactus.steamroulette.data.net.adapters.SystemRequirementsMoshiAdapter
import javax.inject.Named

@Module
abstract class MoshiModule {

    @Module
    companion object {
        @JvmStatic
        @Reusable
        @Named("api")
        @Provides
        fun provideMoshiForApi(): Moshi = Moshi.Builder()
            .add(RequiredAgeMoshiAdapter())
            .add(SystemRequirementsMoshiAdapter())
            .build()

        @Reusable
        @JvmStatic
        @Provides
        fun provideMoshi(): Moshi = Moshi.Builder().build()
    }
}