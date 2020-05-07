package ru.luckycactus.steamroulette.di.common

import android.content.Context
import android.content.res.AssetManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.local.db.DB
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
        fun provideSteamRouletteDb(@ForApplication appContext: Context): DB {
            val migration2To3 = object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE owned_game ADD COLUMN shown INTEGER DEFAULT 0 NOT NULL");
                }
            }
            return Room.databaseBuilder(appContext, DB::class.java, "steam_roulette_db")
                .fallbackToDestructiveMigrationFrom(1)
                .addMigrations(migration2To3)
                .build()
        }

        @JvmStatic
        @Provides
        fun provideAssets(@ForApplication context: Context): AssetManager = context.assets
    }
}