package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.withTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.repositories.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.chunkBuffer
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class LocalGamesDataStore @Inject constructor(
    private val db: DB
) : GamesDataStore.Local {

    override fun observeOwnedGamesCount(steamId: SteamId): LiveData<Int> =
        db.ownedGamesDao().observeCount(steamId.asSteam64())

    override fun observeHiddenOwnedGamesCount(steamId: SteamId): LiveData<Int> =
        db.ownedGamesDao().observeHiddenCount(steamId.asSteam64())

    override suspend fun saveOwnedGames(
        steamId: SteamId,
        gamesFlow: Flow<OwnedGameEntity>
    ) {
        db.withTransaction {
            val gameIds = db.ownedGamesDao().getAllIds(steamId.asSteam64()).toSet()
            val hiddenGameIds = db.ownedGamesDao().getHiddenIds(steamId.asSteam64()).toSet()
            val shownGameIds = db.ownedGamesDao().getShownIds(steamId.asSteam64()).toSet()
            val mapper = OwnedGameRoomEntityMapper(steamId.asSteam64(), hiddenGameIds, shownGameIds)

            db.ownedGamesDao().deleteAll(steamId.asSteam64())

            val gamesVerifier = GamesVerifier(gameIds, false)
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

    override suspend fun getVisibleOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter,
        shown: Boolean
    ): List<Int> = db.ownedGamesDao().run {
        when (filter) {
            PlaytimeFilter.All -> getVisibleIds(steamId.asSteam64(), shown)
            PlaytimeFilter.NotPlayed -> getVisibleLimitedByPlaytimeIds(
                steamId.asSteam64(),
                0,
                shown
            )
            is PlaytimeFilter.Limited -> getVisibleLimitedByPlaytimeIds(
                steamId.asSteam64(),
                filter.maxTime,
                shown
            )
        }
    }

    override suspend fun resetHiddenOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().resetHidden(steamId.asSteam64())
    }

    override suspend fun getOwnedGameHeader(steamId: SteamId, gameId: Int): GameHeader =
        db.ownedGamesDao().getHeader(steamId.asSteam64(), gameId)

    override suspend fun getOwnedGameHeaders(
        steamId: SteamId,
        gameIds: List<Int>
    ): List<GameHeader> =
        db.ownedGamesDao().getHeaders(steamId.asSteam64(), gameIds)

    override suspend fun setOwnedGamesHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean) {
        db.ownedGamesDao().setHidden(steamId.asSteam64(), gameIds, hide)
    }

    override suspend fun setAllOwnedGamesHidden(steamId: SteamId, hide: Boolean) {
        db.ownedGamesDao().setAllHidden(steamId.asSteam64(), hide)
    }

    override suspend fun setOwnedGamesShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean) {
        db.ownedGamesDao().setShown(steamId.asSteam64(), gameIds, shown)
    }

    override suspend fun setAllOwnedGamesShown(steamId: SteamId, shown: Boolean) {
        db.ownedGamesDao().setAllShown(steamId.asSteam64(), shown)
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        db.ownedGamesDao().isUserHasGames(steamId.asSteam64())

    override suspend fun clearOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().delete(steamId.asSteam64())
    }

    override fun getHiddenGamesDataSourceFactory(steamId: SteamId): DataSource.Factory<Int, GameHeader> {
        return db.ownedGamesDao().getHiddenGamesDataSourceFactory(steamId.asSteam64())
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500
    }
}
