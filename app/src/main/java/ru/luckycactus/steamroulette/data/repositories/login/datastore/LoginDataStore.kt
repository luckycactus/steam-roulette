package ru.luckycactus.steamroulette.data.repositories.login.datastore

interface LoginDataStore {

    suspend fun resolveVanityUrl(vanityUrl: String): Long
}