package ru.luckycactus.steamroulette.di.common

import android.app.Application
import android.content.Context
import android.util.LruCache
import androidx.room.Room
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.luckycactus.steamroulette.data.games.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.datastore.GamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.LoginDataStore
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.user.datastore.LocalUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.RemoteUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.data.user_settings.UserSettingsRepositoryImpl
import ru.luckycactus.steamroulette.di.qualifier.ForApplication
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.games.*
import ru.luckycactus.steamroulette.domain.games_filter.ObservePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.SavePlayTimeFilterUseCase
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import ru.luckycactus.steamroulette.domain.login.SignInUseCase
import ru.luckycactus.steamroulette.domain.login.SignOutUserUseCase
import ru.luckycactus.steamroulette.domain.login.ValidateSteamIdInputUseCase
import ru.luckycactus.steamroulette.domain.user.*
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.roulette.GlideGameCoverLoader
import javax.inject.Singleton

@Module
abstract class AppModule {

//    lateinit var appPreferences: SharedPreferences
//        private set
//
//
//    fun init(app: Application) {
//        appPreferences = appContext.getSharedPreferences("app-prefs", Context.MODE_PRIVATE)
//    }

    @Binds
    @ForApplication
    abstract fun bindContext(application: Application): Context

    @Module
    companion object {

        @Singleton
        @JvmStatic
        @Provides
        fun provideLruCache() = LruCache<String, Any>(50)

        @Singleton
        @JvmStatic
        @Provides
        fun provideResourceManager(
            @ForApplication appContext: Context
        ): ResourceManager = AndroidResourceManager(appContext)

        @Singleton
        @JvmStatic
        @Provides
        fun provideCacheHelper(@ForApplication appContext: Context): CacheHelper =
            CacheHelper(appContext.getSharedPreferences("cache-helper", Context.MODE_PRIVATE))

        @Singleton
        @JvmStatic
        @Provides
        fun provideObserveHiddenGamesClearUseCase(gamesRepository: GamesRepository) =
            ObserveHiddenGamesClearUseCase(gamesRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideClearHiddenGamesUseCase(gamesRepository: GamesRepository) =
            ClearHiddenGamesUseCase(gamesRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideObserveHiddenGamesCountUseCase(gamesRepository: GamesRepository) =
            ObserveHiddenGamesCountUseCase(gamesRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideObservePlayTimeFilterUseCase(userSettingsRepository: UserSettingsRepository) =
            ObservePlayTimeFilterUseCase(userSettingsRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideSavePlayTimeFilterUseCase(userSettingsRepository: UserSettingsRepository) =
            SavePlayTimeFilterUseCase(userSettingsRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideObserveOwnedGamesCountUseCase(gamesRepository: GamesRepository) =
            ObserveOwnedGamesCountUseCase(gamesRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideObserveOwnedGamesSyncsUseCase(gamesRepository: GamesRepository) =
            ObserveOwnedGamesSyncsUseCase(gamesRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideObserveCurrentUserSteamIdUseCase(userRepository: UserRepository) =
            ObserveCurrentUserSteamIdUseCase(userRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideObserveUserSummaryUseCase(userRepository: UserRepository) =
            ObserveUserSummaryUseCase(userRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideFetchUserSummaryUseCase(userRepository: UserRepository) =
            FetchUserSummaryUseCase(userRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideFetchUserOwnedGamesUseCase(gamesRepository: GamesRepository) =
            FetchUserOwnedGamesUseCase(gamesRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideGetCurrentUserSteamIdUseCase(userRepository: UserRepository) =
            GetCurrentUserSteamIdUseCase(userRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideGetOwnedGamesQueueUseCase(
            gamesRepository: GamesRepository,
            glideGameCoverLoader: GlideGameCoverLoader
        ) =
            GetLocalOwnedGamesQueueUseCase(
                gamesRepository,
                glideGameCoverLoader
            )

        @Singleton
        @JvmStatic
        @Provides
        fun providesignOutUserUserCase(
            userRepository: UserRepository,
            gamesRepository: GamesRepository,
            userSettingsRepository: UserSettingsRepository,
            glideGameCoverLoader: GlideGameCoverLoader
        ) =
            SignOutUserUseCase(
                userRepository,
                gamesRepository,
                userSettingsRepository,
                glideGameCoverLoader
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideValidateSteamIdInputUseCase() =
            ValidateSteamIdInputUseCase()


        @Singleton
        @JvmStatic
        @Provides
        fun provideSignInUseCase(
            getUserSummaryUseCase: GetUserSummaryUseCase,
            userRepository: UserRepository,
            loginRepository: LoginRepository
        ) =
            SignInUseCase(
                getUserSummaryUseCase,
                userRepository,
                loginRepository
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideGetUserSummaryUseCase(userRepository: UserRepository) =
            GetUserSummaryUseCase(userRepository)

        @Singleton
        @JvmStatic
        @Provides
        fun provideUserRepository(
            localUserDataStore: UserDataStore.Local,
            remoteUserDataStore: UserDataStore.Remote
        ): UserRepository =
            UserRepositoryImpl(
                localUserDataStore,
                remoteUserDataStore,
                UserSummaryMapper()
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideUserSettingsRepository(@ForApplication appContext: Context): UserSettingsRepository =
            UserSettingsRepositoryImpl(
                appContext.getSharedPreferences(
                    "user-settings",
                    Context.MODE_PRIVATE
                )
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideRemoteUserDataStore(steamApiService: SteamApiService): UserDataStore.Remote =
            RemoteUserDataStore(steamApiService)

        @Singleton
        @JvmStatic
        @Provides
        fun provideLocalUserDataStore(
            steamRouletteDb: DB,
            @ForApplication appContext: Context
        ): UserDataStore.Local =
            LocalUserDataStore(
                steamRouletteDb,
                appContext.getSharedPreferences("user-cache", Context.MODE_PRIVATE)
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideLoginRepository(loginDataStore: LoginDataStore): LoginRepository =
            LoginRepositoryImpl(loginDataStore)

        @Singleton
        @JvmStatic
        @Provides
        fun provideLoginDataStore(steamApiService: SteamApiService): LoginDataStore =
            RemoteLoginDataStore(steamApiService)

        @Singleton
        @JvmStatic
        @Provides
        fun provideGamesRepository(
            localGamesDataStore: GamesDataStore.Local,
            remoteGamesDataStore: GamesDataStore.Remote
        ): GamesRepository =
            GamesRepositoryImpl(
                localGamesDataStore,
                remoteGamesDataStore
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideLocalGamesDataStore(steamRouletteDb: DB): GamesDataStore.Local =
            LocalGamesDataStore(steamRouletteDb)

        @Singleton
        @JvmStatic
        @Provides
        fun provideRemoteGamesDataStore(
            steamApiService: SteamApiService,
            gson: Gson
        ): GamesDataStore.Remote =
            RemoteGamesDataStore(
                steamApiService,
                gson
            )

        @Singleton
        @JvmStatic
        @Provides
        fun provideSteamRouletteDb(@ForApplication appContext: Context) =
            Room.databaseBuilder(appContext, DB::class.java, "steam_roulette_db")
                .fallbackToDestructiveMigrationFrom(1)
                .build()


        @Singleton
        @JvmStatic
        @Provides
        fun provideGson() = Gson()
    }
}