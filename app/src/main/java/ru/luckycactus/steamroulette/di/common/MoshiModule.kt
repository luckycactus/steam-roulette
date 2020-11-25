package ru.luckycactus.steamroulette.di.common

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.luckycactus.steamroulette.data.net.adapters.PlaytimeFilterAdapter
import ru.luckycactus.steamroulette.data.net.adapters.RequiredAgeMoshiAdapter
import ru.luckycactus.steamroulette.data.net.adapters.SystemRequirementsMoshiAdapter

@Module
@InstallIn(SingletonComponent::class)
object MoshiModule {
    @Reusable
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(PlaytimeFilterAdapter())
        .add(RequiredAgeMoshiAdapter())
        .add(SystemRequirementsMoshiAdapter())
        .build()
}