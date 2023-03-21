package ru.luckycactus.steamroulette.data.repositories.games.details.datasource

import ru.luckycactus.steamroulette.data.net.api.SteamStoreApiService
import ru.luckycactus.steamroulette.data.repositories.games.details.models.GameStoreInfoEntity
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import javax.inject.Inject

class RemoteGameStoreDataSource @Inject constructor(
    private val steamStoreApiService: SteamStoreApiService,
    private val languageProvider: LanguageProvider,
) : GameStoreDataSource.Remote {

    override suspend fun get(appId: Int): GameStoreInfoEntity? {
        val results = steamStoreApiService.getGamesStoreInfo(
            listOf(appId),
            languageProvider.getLanguageForStoreApi()
        )

        val result = results[appId] ?: return null

        if (!result.success || result.gameStoreInfo == null) {
            return null
        }

        return result.gameStoreInfo
    }
}