package ru.luckycactus.steamroulette.data.login

import ru.luckycactus.steamroulette.domain.entity.SteamId

interface LoginRepository {

    suspend fun resolveVanityUrl(vanityUrl: String): SteamId
}
