package ru.luckycactus.steamroulette.data.repositories.login.datasource

import ru.luckycactus.steamroulette.data.net.api.SteamApiService
import ru.luckycactus.steamroulette.domain.login.VanityNotFoundException
import javax.inject.Inject

class RemoteLoginDataSource @Inject constructor(
    private val steamApiService: SteamApiService
) : LoginDataSource {

    override suspend fun resolveVanityUrl(vanityUrl: String): Long {
        val result = steamApiService.resolveVanityUrl(vanityUrl).result

        if (result.success == 1 && !result.steamId.isNullOrEmpty())
            return result.steamId.toLong()
        else
            throw VanityNotFoundException(vanityUrl)
    }
}

