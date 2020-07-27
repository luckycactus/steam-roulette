package ru.luckycactus.steamroulette.di.common

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import ru.luckycactus.steamroulette.data.net.adapters.RequiredAgeMoshiAdapter
import ru.luckycactus.steamroulette.data.net.adapters.SystemRequirementsMoshiAdapter
import javax.inject.Named

@Module
@InstallIn(ApplicationComponent::class)
abstract class MoshiModule {

    companion object {
        @Reusable
        @Named("api")
        @Provides
        fun provideMoshiForApi(): Moshi = Moshi.Builder()
            .add(RequiredAgeMoshiAdapter())
            .add(SystemRequirementsMoshiAdapter())
            .build()

        @Reusable
        @Provides
        fun provideMoshi(): Moshi = Moshi.Builder().build()
    }
}