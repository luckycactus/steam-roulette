package ru.luckycactus.steamroulette.data.repositories.login.datastore

import ru.luckycactus.steamroulette.domain.login.VanityNotFoundException

interface LoginDataStore {

    @Throws(VanityNotFoundException::class)
    suspend fun resolveVanityUrl(vanityUrl: String): Long
}