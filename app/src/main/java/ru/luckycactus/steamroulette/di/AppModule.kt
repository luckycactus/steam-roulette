package ru.luckycactus.steamroulette.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.LruCache
import androidx.room.Room
import ru.luckycactus.steamroulette.data.games.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.user.datastore.LocalUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.RemoteUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.data.user_settings.UserSettingsRepositoryImpl
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

object AppModule {

    lateinit var appContext: Context
    lateinit var cacheHelper: CacheHelper
        private set
    lateinit var requestLruCache: LruCache<String, Any>
        private set
    lateinit var appPreferences: SharedPreferences
        private set

    lateinit var resourceManager: ResourceManager
        private set

    fun init(app: Application) {
        appContext = app
        cacheHelper =
            CacheHelper(appContext.getSharedPreferences("cache-helper", Context.MODE_PRIVATE))
        appPreferences = appContext.getSharedPreferences("app-prefs", Context.MODE_PRIVATE)
        resourceManager = AndroidResourceManager(appContext)
        requestLruCache = LruCache(50)
    }

    val observeHiddenGamesClearUseCase by lazy {
        ObserveHiddenGamesClearUseCase(gamesRepository)
    }

    val clearHiddenGamesUseCase by lazy {
        ClearHiddenGamesUseCase(gamesRepository)
    }

    val observeHiddenGamesCountUseCase by lazy {
        ObserveHiddenGamesCountUseCase(gamesRepository)
    }

    val observePlayTimeFilterUseCase by lazy {
        ObservePlayTimeFilterUseCase(userSettingsRepository)
    }

    val savePlayTimeFilterUseCase by lazy {
        SavePlayTimeFilterUseCase(userSettingsRepository)
    }

    val observeOwnedGamesCountUseCase by lazy {
        ObserveOwnedGamesCountUseCase(gamesRepository)
    }

    val observeOwnedGamesSyncsUseCase by lazy {
        ObserveOwnedGamesSyncsUseCase(gamesRepository)
    }

    val observeCurrentUserSteamIdUseCase by lazy {
        ObserveCurrentUserSteamIdUseCase(
            userRepository
        )
    }

    val observeUserSummaryUseCase by lazy {
        ObserveUserSummaryUseCase(
            userRepository
        )
    }

    val fetchUserSummaryUseCase by lazy {
        FetchUserSummaryUseCase(
            userRepository
        )
    }

    val fetchUserOwnedGamesUseCase by lazy {
        FetchUserOwnedGamesUseCase(
            gamesRepository
        )
    }

//    val updateUserAndGamesUseCase by lazy {
//        UpdateUserAndGamesUseCase(
//            userRepository,
//            gamesRepository
//        )
//    }

    val getSignedInUserSteamIdUseCase by lazy {
        GetCurrentUserSteamIdUseCase(
            userRepository
        )
    }

    val getOwnedGamesQueueUseCase by lazy {
        GetLocalOwnedGamesQueueUseCase(
            gamesRepository
        )
    }

    val signOutUserUserCase by lazy {
        SignOutUserUseCase(userRepository)
    }

    val validateSteamIdInputUseCase by lazy {
        ValidateSteamIdInputUseCase()
    }

    val signInUseCase by lazy {
        SignInUseCase(
            getUserSummaryUseCase,
            userRepository,
            loginRepository
        )
    }

    val getUserSummaryUseCase by lazy {
        GetUserSummaryUseCase(userRepository)
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            localUserDataStore,
            remoteUserDataStore,
            UserSummaryMapper()
        )
    }

    private val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepositoryImpl(
            appContext.getSharedPreferences(
                "user-settings",
                Context.MODE_PRIVATE
            )
        )
    }

    private val remoteUserDataStore: UserDataStore.Remote by lazy {
        RemoteUserDataStore(
            NetworkModule.steamApiService
        )
    }

    private val localUserDataStore: UserDataStore.Local by lazy {
        LocalUserDataStore(
            steamRouletteDb,
            appContext.getSharedPreferences("user-cache", Context.MODE_PRIVATE)
        )
    }

    private val loginRepository: LoginRepository by lazy {
        LoginRepositoryImpl(remoteLoginDataStore)
    }

    private val remoteLoginDataStore: RemoteLoginDataStore by lazy {
        RemoteLoginDataStore(NetworkModule.steamApiService)
    }

    private val gamesRepository: GamesRepository by lazy {
        GamesRepositoryImpl(
            LocalGamesDataStore,
            RemoteGamesDataStore
        )
    }

    private val LocalGamesDataStore: LocalGamesDataStore by lazy {
        LocalGamesDataStore(steamRouletteDb)
    }

    private val RemoteGamesDataStore: RemoteGamesDataStore by lazy {
        RemoteGamesDataStore(NetworkModule.steamApiService)
    }

    private val ownedGameMapper by lazy {
        OwnedGameMapper()
    }

    private val steamRouletteDb by lazy {
        Room.databaseBuilder(appContext, DB::class.java, "steam_roulette_db")
            .fallbackToDestructiveMigration() //todo remove
            .build()
    }
}