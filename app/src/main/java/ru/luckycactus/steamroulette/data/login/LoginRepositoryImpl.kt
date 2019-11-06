package ru.luckycactus.steamroulette.data.login

import ru.luckycactus.steamroulette.data.login.datastore.LoginDataStore
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.login.LoginRepository

class LoginRepositoryImpl(
    private val loginDataStore: LoginDataStore
) : LoginRepository {

    override suspend fun resolveVanityUrl(vanityUrl: String): SteamId =
        SteamId.fromSteam64(loginDataStore.resolveVanityUrl(vanityUrl))

}