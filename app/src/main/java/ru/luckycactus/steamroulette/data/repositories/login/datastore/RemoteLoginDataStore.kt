package ru.luckycactus.steamroulette.data.repositories.login.datastore

import dagger.Reusable
import ru.luckycactus.steamroulette.data.core.wrapCommonNetworkExceptions
import ru.luckycactus.steamroulette.data.net.services.SteamApiService
import ru.luckycactus.steamroulette.domain.login.VanityNotFoundException
import javax.inject.Inject

@Reusable
class RemoteLoginDataStore @Inject constructor(
    private val steamApiService: SteamApiService
) : LoginDataStore {

    override suspend fun resolveVanityUrl(vanityUrl: String): Long {
        val result = wrapCommonNetworkExceptions {
            steamApiService.resolveVanityUrl(vanityUrl).result
        }

        if (result.success == 1 && !result.steamId.isNullOrEmpty())
            return result.steamId.toLong()
        else
            throw VanityNotFoundException(vanityUrl)
    }
}

