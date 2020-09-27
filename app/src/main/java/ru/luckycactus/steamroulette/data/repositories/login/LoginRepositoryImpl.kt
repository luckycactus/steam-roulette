package ru.luckycactus.steamroulette.data.repositories.login

import dagger.Reusable
import ru.luckycactus.steamroulette.data.repositories.login.datasource.LoginDataSource
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import javax.inject.Inject

@Reusable
class LoginRepositoryImpl @Inject constructor(
    private val loginDataSource: LoginDataSource
) : LoginRepository {

    override suspend fun resolveVanityUrl(vanityUrl: String): SteamId =
        SteamId.fromSteam64(loginDataSource.resolveVanityUrl(vanityUrl))

}