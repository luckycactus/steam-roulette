package ru.luckycactus.steamroulette.data.user.datastore

import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.domain.exception.SteamIdNotFoundException

class RemoteUserDataStore(
    private val steamApiService: SteamApiService
) : UserDataStore.Remote {

    override suspend fun getUserSummary(steam64: Long): UserSummaryEntity {
        val response = wrapCommonNetworkExceptions {
            steamApiService.getUserSummaries(listOf(steam64))
        }

        return response.result.players.getOrNull(0) ?: throw SteamIdNotFoundException(
            steam64.toString()
        )
    }
}