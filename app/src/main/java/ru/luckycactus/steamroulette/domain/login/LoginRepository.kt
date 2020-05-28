package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.domain.common.SteamId

interface LoginRepository {
    @Throws(VanityNotFoundException::class)
    suspend fun resolveVanityUrl(vanityUrl: String): SteamId
}

class VanityNotFoundException(val vanity: String) : Exception()