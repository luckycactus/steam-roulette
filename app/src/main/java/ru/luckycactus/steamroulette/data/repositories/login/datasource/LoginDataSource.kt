package ru.luckycactus.steamroulette.data.repositories.login.datasource

import ru.luckycactus.steamroulette.domain.login.VanityNotFoundException

interface LoginDataSource {

    @Throws(VanityNotFoundException::class)
    suspend fun resolveVanityUrl(vanityUrl: String): Long
}