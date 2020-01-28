package ru.luckycactus.steamroulette.di.common

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.repositories.games.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games.datastore.GamesDataStore
import ru.luckycactus.steamroulette.data.repositories.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.repositories.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.LanguageProviderImpl
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.repositories.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.login.datastore.LoginDataStore
import ru.luckycactus.steamroulette.data.repositories.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.repositories.update.AppSettingsRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.datastore.LocalUserDataStore
import ru.luckycactus.steamroulette.data.repositories.user.datastore.RemoteUserDataStore
import ru.luckycactus.steamroulette.data.repositories.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.repositories.user_settings.UserSettingsRepositoryImpl
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.di.qualifier.Identified
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.common.GameCoverCacheCleaner
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import ru.luckycactus.steamroulette.domain.update.AppSettingsRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideGameCoverCacheCleaner
import javax.inject.Singleton

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class])
abstract class AppModule {

    @Binds
    @ForApplication
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindResourceManager(androidResourceManager: AndroidResourceManager): ResourceManager

    @Binds
    abstract fun bindAppSettingsRepository(appSettingsRepositoryImpl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds
    abstract fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindUserSettingsRepository(userSettingsRepository: UserSettingsRepositoryImpl): UserSettingsRepository

    @Binds
    abstract fun bindLoginRepository(loginRepository: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindRemoteUserDataStore(dataStore: RemoteUserDataStore): UserDataStore.Remote

    @Binds
    abstract fun bindGamesRepository(gamesRepository: GamesRepositoryImpl): GamesRepository

    @Binds
    abstract fun bindGameCoverCacheCleaner(glideGameCoverCacheCleaner: GlideGameCoverCacheCleaner): GameCoverCacheCleaner

    @Binds
    abstract fun bindRemoteGamesDataStore(remoteGamesDataStore: RemoteGamesDataStore): GamesDataStore.Remote

    @Binds
    abstract fun bindLocalGamesDataStore(localGamesDataStore: LocalGamesDataStore): GamesDataStore.Local

    @Binds
    abstract fun bindLocalUserDataStore(localUserDataStore: LocalUserDataStore): UserDataStore.Local

    @Binds
    abstract fun bindLoginDataStore(loginDataStore: RemoteLoginDataStore): LoginDataStore

    @Binds
    abstract fun bindLanguageProvider(languageProviderImpl: LanguageProviderImpl): LanguageProvider

    @Module
    companion object {
        @Identified(R.id.appPrefs)
        @JvmStatic
        @Singleton
        @Provides
        fun provideAppsSharedPreferences(@ForApplication appContext: Context) =
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
        fun provideSteamRouletteDb(@ForApplication appContext: Context) =
            Room.databaseBuilder(appContext, DB::class.java, "steam_roulette_db")
                .fallbackToDestructiveMigrationFrom(1)
                .addMigrations()
                .build()


        @Reusable
        @JvmStatic
        @Provides
        fun provideGson() = Gson()
    }
}