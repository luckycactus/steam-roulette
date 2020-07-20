package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId

interface UserSessionRepository {
    fun setCurrentUser(steamId: SteamId)

    val currentUser: SteamId?

    //todo user remove?
    fun observeCurrentUser(): Flow<SteamId?>

    fun isUserLoggedIn(): Boolean

    suspend fun logOut()
}