package ru.luckycactus.steamroulette.presentation.main

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.di.qualifier.ForActivity
import ru.luckycactus.steamroulette.di.scopes.ActivityScope
import ru.luckycactus.steamroulette.domain.common.ResourceManager

@Module
abstract class MainActivityModule {

    @Binds
    @ForActivity
    abstract fun activityContext(activity: MainActivity): Context

    @Module
    companion object {

        @ActivityScope
        @JvmStatic
        @Provides
        fun provideResourceManager(
            @ForActivity context: Context
        ): ResourceManager = AndroidResourceManager(context)
    }
}