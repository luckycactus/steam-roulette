package ru.luckycactus.steamroulette.data.login.datastore

interface LoginDataStore {

    suspend fun resolveVanityUrl(vanityUrl: String): Long
}