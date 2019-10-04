package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.SteamId
import java.util.*

interface GamesRepository {

    suspend fun getOwnedGames(steamId: SteamId, cachePolicy: CachePolicy): List<OwnedGame>

    suspend fun fetchOwnedGames(steamId: SteamId, cachePolicy: CachePolicy)

    suspend fun getFilteredLocalOwnedGamesIds(
        steamId: SteamId,
        filter: EnPlayTimeFilter
    ): List<Int>

    suspend fun getLocalOwnedGame(steamId: SteamId, appId: Int): OwnedGame

    suspend fun markLocalGameAsHidden(steamId: SteamId, ownedGame: OwnedGame)

    suspend fun isUserHasLocalOwnedGames(steamId: SteamId): Boolean

    fun observeGamesCount(steamId: SteamId): LiveData<Int>

    fun observeGamesUpdates(steamId: SteamId): LiveData<Date>
}
