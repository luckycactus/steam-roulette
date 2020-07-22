package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.paging.DataSource
import androidx.room.withTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.chunkBuffer
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class LocalGamesDataStore @Inject constructor(
    private val db: AppDatabase,
    private val gamesVerifierFactory: GamesVerifier.Factory
) : GamesDataStore.Local {

    override fun observeOwnedGamesCount(steamId: SteamId): Flow<Int> =
        db.ownedGamesDao().observeCount(steamId.as64())

    override fun observeHiddenOwnedGamesCount(steamId: SteamId): Flow<Int> =
        db.ownedGamesDao().observeHiddenCount(steamId.as64())

    override suspend fun updateOwnedGames(
        steamId: SteamId,
        gamesFlow: Flow<OwnedGameEntity>
    ) {
        val steam64 = steamId.as64()
        db.withTransaction {
            val appData = db.ownedGamesDao().getAllAppData(steam64).associateBy { it.appId }
            val mapper = OwnedGameRoomEntityMapper(steam64, appData)
            val gamesVerifier = gamesVerifierFactory.create(appData.keys)

            db.ownedGamesDao().clear(steam64)

            gamesFlow
                .filter { gamesVerifier.verify(it) }
                .map { mapper.mapFrom(it) }
                .chunkBuffer(GAMES_BUFFER_SIZE)
                .collect {
                    db.ownedGamesDao().insert(it)
                }
            gamesVerifier.log()
        }
    }

    override suspend fun getOwnedGames(steamId: SteamId): List<OwnedGameEntity> {
        return db.ownedGamesDao().getAll(steamId.as64())
    }

    override suspend fun getOwnedGamesIds(
        steamId: SteamId,
        shown: Boolean?,
        hidden: Boolean?,
        filter: PlaytimeFilter?
    ): List<Int> {
        val maxHours = filter?.let {
            when (filter) {
                PlaytimeFilter.All -> null
                PlaytimeFilter.NotPlayed -> 0
                is PlaytimeFilter.Limited -> filter.maxHours
            }
        }
        return db.ownedGamesDao().getIds(steamId.as64(), shown, hidden, maxHours)
    }

    override suspend fun resetHiddenOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().resetHidden(steamId.as64())
    }

    override suspend fun getOwnedGameHeader(steamId: SteamId, gameId: Int): GameHeader =
        db.ownedGamesDao().getHeader(steamId.as64(), gameId)

    override suspend fun getOwnedGameHeaders(
        steamId: SteamId,
        gameIds: List<Int>
    ): List<GameHeader> = db.ownedGamesDao().getHeaders(steamId.as64(), gameIds)

    override suspend fun setOwnedGamesHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean) {
        db.ownedGamesDao().setHidden(steamId.as64(), gameIds, hide)
    }

    override suspend fun setAllOwnedGamesHidden(steamId: SteamId, hide: Boolean) {
        db.ownedGamesDao().setAllHidden(steamId.as64(), hide)
    }

    override suspend fun setOwnedGamesShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean) {
        db.ownedGamesDao().setShown(steamId.as64(), gameIds, shown)
    }

    override suspend fun setAllOwnedGamesShown(steamId: SteamId, shown: Boolean) {
        db.ownedGamesDao().setAllShown(steamId.as64(), shown)
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        db.ownedGamesDao().isUserHasGames(steamId.as64())

    override suspend fun clearOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().clear(steamId.as64())
    }

    override fun getHiddenGamesDataSourceFactory(steamId: SteamId): DataSource.Factory<Int, GameHeader> {
        return db.ownedGamesDao().getHiddenGamesDataSourceFactory(steamId.as64())
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500
    }
}
