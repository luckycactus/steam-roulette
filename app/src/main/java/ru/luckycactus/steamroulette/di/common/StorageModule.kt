package ru.luckycactus.steamroulette.di.common

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.di.Identified
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class StorageModule {

    companion object {
        @Identified(R.id.appPrefs)
        @Singleton
        @Provides
        fun provideAppSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("app-prefs", Context.MODE_PRIVATE)

        @Identified(R.id.userSettingsPrefs)
        @Singleton
        @Provides
        fun provideUserSettingsSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("user-settings", Context.MODE_PRIVATE)

        @Identified(R.id.userCachePrefs)
        @Singleton
        @Provides
        fun provideUserCacheSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("user-cache", Context.MODE_PRIVATE)

        @Identified(R.id.cacheHelperPrefs)
        @Singleton
        @Provides
        fun provideCacheHelperSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("cache-helper", Context.MODE_PRIVATE)

        @Singleton
        @Provides
        fun provideSteamRouletteDb(@ApplicationContext appContext: Context) =
            AppDatabase.buildDatabase(appContext)

        @Provides
        fun provideAssets(@ApplicationContext context: Context): AssetManager = context.assets
    }
}