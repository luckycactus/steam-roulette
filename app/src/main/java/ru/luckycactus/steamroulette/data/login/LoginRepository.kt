package ru.luckycactus.steamroulette.data.login

import ru.luckycactus.steamroulette.domain.user.SteamId

interface LoginRepository {

    suspend fun resolveVanityUrl(vanityUrl: String): SteamId
}
