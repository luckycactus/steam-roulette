package ru.luckycactus.steamroulette.data.repositories.user.datastore

import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.data.net.services.SteamApiService
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.SteamIdNotFoundException
import javax.inject.Inject

@Reusable
class RemoteUserDataStore @Inject constructor(
    private val steamApiService: SteamApiService
) : UserDataStore.Remote {

    @Throws(SteamIdNotFoundException::class)
    override suspend fun getUserSummary(steamId: SteamId): UserSummaryEntity {
        val response = wrapCommonNetworkExceptions {
            steamApiService.getUserSummaries(listOf(steamId.as64()))
        }

        return response.result.players.getOrNull(0)
            ?: throw SteamIdNotFoundException(steamId.toString())
    }
}