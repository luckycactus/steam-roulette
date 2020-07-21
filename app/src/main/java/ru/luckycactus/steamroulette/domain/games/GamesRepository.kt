package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

interface GamesRepository {

    @Throws(GetOwnedGamesPrivacyException::class)
    suspend fun fetchOwnedGames(cachePolicy: CachePolicy)

    suspend fun getVisibleLocalOwnedGamesIds(
        filter: PlaytimeFilter,
        shown: Boolean
    ): List<Int>

    suspend fun getLocalOwnedGameHeaders(gameIds: List<Int>): List<GameHeader>

    suspend fun setLocalOwnedGamesHidden(gameIds: List<Int>, hide: Boolean)

    suspend fun setAllLocalOwnedGamesHidden(hide: Boolean)

    suspend fun setLocalOwnedGamesShown(gameIds: List<Int>, shown: Boolean)

    suspend fun setAllLocalOwnedGamesShown(shown: Boolean)

    suspend fun isUserHasGames(): Boolean

    fun observeGamesCount(): Flow<Int>

    fun observeHiddenGamesCount(): Flow<Int>

    //todo flow
    fun getHiddenGamesPagedListLiveData(): LiveData<PagedList<GameHeader>>

    suspend fun resetHiddenGames()

    fun observeGamesUpdates(): Flow<Long>

    suspend fun clearUser(steamId: SteamId)

    @Throws(GetGameStoreInfoException::class)
    suspend fun getGameStoreInfo(gameId: Int, cachePolicy: CachePolicy): GameStoreInfo?
}

class GetOwnedGamesPrivacyException: Exception()

class GetGameStoreInfoException: Exception()

