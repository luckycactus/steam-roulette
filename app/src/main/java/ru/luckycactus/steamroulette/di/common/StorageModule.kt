package ru.luckycactus.steamroulette.di.common

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.di.qualifier.Identified
import javax.inject.Singleton

@Module
class StorageModule {

    @Module
    companion object {
        @Identified(R.id.appPrefs)
        @JvmStatic
        @Singleton
        @Provides
        fun provideAppSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("app-prefs", Context.MODE_PRIVATE)

        @Identified(R.id.userSettingsPrefs)
        @JvmStatic
        @Singleton
        @Provides
        fun provideUserSettingsSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("user-settings", Context.MODE_PRIVATE)

        @Identified(R.id.userCachePrefs)
        @JvmStatic
        @Singleton
        @Provides
        fun provideUserCacheSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("user-cache", Context.MODE_PRIVATE)

        @Identified(R.id.cacheHelperPrefs)
        @JvmStatic
        @Singleton
        @Provides
        fun provideCacheHelperSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("cache-helper", Context.MODE_PRIVATE)

        @Singleton
        @JvmStatic
        @Provides
        fun provideSteamRouletteDb(@ForApplication appContext: Context) = AppDatabase.buildDatabase(appContext)

        @JvmStatic
        @Provides
        fun provideAssets(@ForApplication context: Context): AssetManager = context.assets
    }
}