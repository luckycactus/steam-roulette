package ru.luckycactus.steamroulette.di.common

import android.app.Application
import android.content.Context
import android.util.LruCache
import androidx.room.Room
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.games.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.datastore.GamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.LoginDataStore
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.user.datastore.LocalUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.RemoteUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user_settings.UserSettingsRepositoryImpl
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.entity.GameCoverCacheCleaner
import ru.luckycactus.steamroulette.domain.entity.GameCoverPreloader
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.roulette.GlideGameCoverLoader
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    @ForApplication
    abstract fun bindContext(application: Application): Context

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
    abstract fun bindGameCoverPreloader(glideGameCoverLoader: GlideGameCoverLoader): GameCoverPreloader

    @Binds
    abstract fun bindGameCoverCacheCleaner(glideGameCoverLoader: GlideGameCoverLoader): GameCoverCacheCleaner

    @Binds
    abstract fun bindRemoteGamesDataStore(remoteGamesDataStore: RemoteGamesDataStore): GamesDataStore.Remote

    @Binds
    abstract fun bindLocalGamesDataStore(localGamesDataStore: LocalGamesDataStore): GamesDataStore.Local

    @Binds
    abstract fun bindLocalUserDataStore(localUserDataStore: LocalUserDataStore): UserDataStore.Local

    @Binds
    abstract fun bindLoginDataStore(loginDataStore: RemoteLoginDataStore): LoginDataStore

    @Module
    companion object {

        //todo merge userSettingsPrefs and userCachePrefs
        @Identified(R.id.userSettingsPrefs)
        @JvmStatic
        @Provides
        fun provideUserSettingsSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("user-settings", Context.MODE_PRIVATE)

        @Identified(R.id.userCachePrefs)
        @JvmStatic
        @Provides
        fun provideUserCacheSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("user-cache", Context.MODE_PRIVATE)

        @Identified(R.id.cacheHelperPrefs)
        @JvmStatic
        @Provides
        fun provideCacheHelperSharedPreferences(@ForApplication appContext: Context) =
            appContext.getSharedPreferences("cache-helper", Context.MODE_PRIVATE)

        @Singleton
        @JvmStatic
        @Provides
        fun provideSteamRouletteDb(@ForApplication appContext: Context) =
            Room.databaseBuilder(appContext, DB::class.java, "steam_roulette_db")
                .fallbackToDestructiveMigrationFrom(1)
                .build()


        @Reusable
        @JvmStatic
        @Provides
        fun provideGson() = Gson()
    }
}