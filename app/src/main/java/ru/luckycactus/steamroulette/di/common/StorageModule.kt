package ru.luckycactus.steamroulette.di.common

import android.content.Context
import android.content.res.AssetManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.luckycactus.steamroulette.data.core.CacheHelper
import ru.luckycactus.steamroulette.data.core.RoomCacheHelper
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class StorageModule {

    @Binds
    abstract fun bindRoomCacheHelper(cacheHelperImpl: RoomCacheHelper): CacheHelper

    companion object {
        @Named("app")
        @Singleton
        @Provides
        fun provideAppSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("app-prefs", Context.MODE_PRIVATE)

        @Named("roulette-filters")
        @Singleton
        @Provides
        fun provideRoulettePlayTimeSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("user-settings", Context.MODE_PRIVATE)

        @Named("library-filters")
        @Singleton
        @Provides
        fun provideLibraryPlayTimeSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("library-filters", Context.MODE_PRIVATE)

        @Named("user-cache")
        @Singleton
        @Provides
        fun provideUserCacheSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("user-cache", Context.MODE_PRIVATE)

        @Named("cache-helper")
        @Singleton
        @Provides
        fun provideCacheHelperSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("cache-helper", Context.MODE_PRIVATE)

        @Named("app-review")
        @Singleton
        @Provides
        fun provideAppReviewSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("app-review", Context.MODE_PRIVATE)

        @Named("library-settings")
        @Singleton
        @Provides
        fun provideLibrarySettingsSharedPreferences(@ApplicationContext appContext: Context) =
            appContext.getSharedPreferences("library-settings", Context.MODE_PRIVATE)

        @Singleton
        @Provides
        fun provideSteamRouletteDb(@ApplicationContext appContext: Context) =
            AppDatabase.buildDatabase(appContext)

        @Provides
        fun provideAssets(@ApplicationContext context: Context): AssetManager = context.assets
    }
}