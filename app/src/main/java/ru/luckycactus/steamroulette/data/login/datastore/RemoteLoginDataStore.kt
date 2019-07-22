package ru.luckycactus.steamroulette.data.login.datastore

import ru.luckycactus.steamroulette.data.net.SteamApiService
import ru.luckycactus.steamroulette.data.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.domain.exception.InvalidVanityException

class RemoteLoginDataStore(
    private val steamApiService: SteamApiService
) : LoginDataStore {

    override suspend fun resolveVanityUrl(vanityUrl: String): Long {
        val result = wrapCommonNetworkExceptions {
            steamApiService.resolveVanityUrl(vanityUrl).result
        }

        if (result.success == 1 && !result.steamId.isNullOrEmpty())
            return result.steamId.toLong()
        else
            throw InvalidVanityException(vanityUrl)
    }
}

