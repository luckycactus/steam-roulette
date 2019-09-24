package ru.luckycactus.steamroulette.data.user

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.presentation.utils.startWith
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(
    private val localUserDataStore: UserDataStore.Local,
    private val remoteUserDataStore: UserDataStore.Remote,
    private val mapper: UserSummaryMapper,
    private val userPreferences: PreferencesStorage //todo replace by room
) : UserRepository {

    private val currentUserSteamId = MutableLiveData<SteamId?>().startWith(getCurrentUserSteamId())
    private val currentUserSummary = MediatorLiveData<UserSummary?>()

    init {
        currentUserSummary.addSource(currentUserSteamId) {
            if (it == null) {
                currentUserSummary.value = null
                return@addSource
            }

            GlobalScope.launch {
                val userSummary = mapper.mapFrom(localUserDataStore.getUserSummary(it.asSteam64()))
                withContext(Dispatchers.Main) {
                    if (currentUserSummary.value == null) {
                        currentUserSummary.value = userSummary
                    }
                }
            }
        }
    }


    //todo move to datastore?
    override fun saveSignedInUser(steamId: SteamId) {
        userPreferences[SIGNED_USER_KEY] = steamId.asSteam64()
        currentUserSteamId.postValue(steamId)
    }

    override fun getCurrentUserSteamId(): SteamId? {
        val steam64 = userPreferences.getLong(SIGNED_USER_KEY)
        if (steam64 == 0L)
            return null
        return SteamId.fromSteam64(steam64)
    }

    override fun observeCurrentUserSteamId(): LiveData<SteamId?> = currentUserSteamId

    override fun isUserSignedIn(): Boolean =
        userPreferences.getLong(SIGNED_USER_KEY) != 0L

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary {
        return createUserSummaryResource(steamId)
            .get(cachePolicy)
    }

    override fun observeCurrentUserSummary(): LiveData<UserSummary?> {
        return currentUserSummary.distinctUntilChanged()
    }

    override suspend fun refreshUserSummary(steamId: SteamId, cachePolicy: CachePolicy) {
        createUserSummaryResource(steamId)
            .updateIfNeed(cachePolicy)
    }

    override suspend fun getUserSummaryCacheThenRemoteIfExpired(
        coroutineScope: CoroutineScope,
        steamId: SteamId
    ): ReceiveChannel<UserSummary> =
        createUserSummaryResource(steamId).getCacheThenRemoteIfExpired(coroutineScope)

    private fun createUserSummaryResource(
        steamId: SteamId
    ): NetworkBoundResource<UserSummaryEntity, UserSummary> {
        val steam64 = steamId.asSteam64()
        val cacheKey = "user_summary_$steam64"
        return object : NetworkBoundResource<UserSummaryEntity, UserSummary>(
            cacheKey,
            cacheKey,
            SUMMARY_CACHE_WINDOW
        ) {
            private var result: UserSummary? = null

            override suspend fun getFromNetwork(): UserSummaryEntity {
                return remoteUserDataStore.getUserSummary(steam64)
            }

            override suspend fun saveToCache(data: UserSummaryEntity) {
                localUserDataStore.saveUserSummaryToCache(data)
                result = mapper.mapFrom(data)
                withContext(Dispatchers.Main) {
                    if (currentUserSteamId.value == steamId) {
                        currentUserSummary.value = result
                    }
                }
            }

            override suspend fun getFromCache(): UserSummary {
                return result ?: mapper.mapFrom(localUserDataStore.getUserSummary(steam64))
            }
        }
    }

    override suspend fun signOut() {
        userPreferences.remove(SIGNED_USER_KEY)
        currentUserSteamId.postValue(null)
    }

    companion object {
        const val SIGNED_USER_KEY = "signed_user_key"
        val SUMMARY_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(4L, TimeUnit.HOURS)
    }
}