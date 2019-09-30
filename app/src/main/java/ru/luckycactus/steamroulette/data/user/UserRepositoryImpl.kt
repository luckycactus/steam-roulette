package ru.luckycactus.steamroulette.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.*
import ru.luckycactus.steamroulette.data.long
import ru.luckycactus.steamroulette.data.longLiveData
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import ru.luckycactus.steamroulette.domain.user.UserRepository
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(
    private val localUserDataStore: UserDataStore.Local,
    private val remoteUserDataStore: UserDataStore.Remote,
    private val mapper: UserSummaryMapper,
    private val userPreferences: SharedPreferences //todo replace by room
) : UserRepository {

    private var currentUserSteam64Pref by userPreferences.long(SIGNED_USER_KEY, 0)

    //todo move to datastore?
    override fun saveSignedInUser(steamId: SteamId) {
        currentUserSteam64Pref = steamId.asSteam64()
    }

    override fun getCurrentUserSteamId() = fromSteam64(currentUserSteam64Pref)

    override fun observeCurrentUserSteamId(): LiveData<SteamId?> =
        userPreferences.longLiveData(SIGNED_USER_KEY, 0).map {
            fromSteam64(it)
        }

    override fun isUserSignedIn(): Boolean =
        userPreferences.getLong(SIGNED_USER_KEY, 0L) != 0L

    override suspend fun getUserSummary(
        steamId: SteamId,
        cachePolicy: CachePolicy
    ): UserSummary {
        return createUserSummaryResource(steamId)
            .get(cachePolicy)
    }

    override fun observeUserSummary(steamId: SteamId): LiveData<UserSummary> {
        return localUserDataStore.observeUserSummary(steamId.asSteam64())
            .map { mapper.mapFrom(it) }
            .distinctUntilChanged()
    }

    override suspend fun refreshUserSummary(steamId: SteamId, cachePolicy: CachePolicy) {
        createUserSummaryResource(steamId).updateIfNeed(cachePolicy)
    }

    override suspend fun signOut() {
        userPreferences.edit { remove(SIGNED_USER_KEY) }
    }

    private fun fromSteam64(steam64: Long) =
        if (steam64 == 0L) null else SteamId.fromSteam64(steam64)

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
            }

            override suspend fun getFromCache(): UserSummary {
                return result ?: mapper.mapFrom(localUserDataStore.getUserSummary(steam64))
            }
        }
    }

    companion object {
        const val SIGNED_USER_KEY = "signed_user_key"
        val SUMMARY_CACHE_WINDOW = TimeUnit.MILLISECONDS.convert(4L, TimeUnit.HOURS)
    }
}