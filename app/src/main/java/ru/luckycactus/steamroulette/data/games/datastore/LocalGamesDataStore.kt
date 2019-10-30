package ru.luckycactus.steamroulette.data.games.datastore

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.utils.chunkBuffer

class LocalGamesDataStore(
    private val db: DB
) : GamesDataStore.Local {

    override fun observeOwnedGameCount(steam64: Long): LiveData<Int> {
        return db.ownedGamesDao().observeGameCount(steam64)
    }

    override fun observeHiddenOwnedGameCount(steam64: Long): LiveData<Int> {
        return db.ownedGamesDao().observeHiddenGameCount(steam64)
    }

    override suspend fun saveOwnedGames(steam64: Long, gamesFlow: Flow<OwnedGameEntity>) {
        db.withTransaction {
            val hiddenGameIds = db.ownedGamesDao().getHiddenGamesIds(steam64).toSet()
            val timestamp = System.currentTimeMillis()
            val mapper = OwnedGameRoomEntityMapper(steam64, hiddenGameIds, timestamp)

            withContext(Dispatchers.Default) {
                gamesFlow
                    .map { mapper.mapFrom(it) }
                    .chunkBuffer(GAMES_BUFFER_SIZE)
                    .collect {
                        db.ownedGamesDao().insertGames(it)
                    }
            }

            db.ownedGamesDao().removeGamesUpdatedEarlierThen(steam64, timestamp)
        }
    }

    override suspend fun getFilteredOwnedGamesIds(
        steam64: Long,
        filter: EnPlayTimeFilter
    ): List<Int> {
        return when (filter) {
            EnPlayTimeFilter.All -> db.ownedGamesDao().getVisibleGamesIdList(steam64)
            EnPlayTimeFilter.NotPlayed -> db.ownedGamesDao().getVisibleNotPlayedGamesIdList(steam64)
            EnPlayTimeFilter.NotPlayedIn2Weeks -> db.ownedGamesDao().getVisibleNotPlayed2WeeksGamesIdList(
                steam64
            )
        }
    }

    override suspend fun clearHiddenOwnedGames(steam64: Long) {
        db.ownedGamesDao().clearHiddenGames(steam64)
    }

    override suspend fun getOwnedGame(steam64: Long, appId: Int): OwnedGame {
        return db.ownedGamesDao().getGame(steam64, appId)
    }

    override suspend fun getOwnedGames(steam64: Long, appIds: List<Int>): List<OwnedGame> {
        return db.ownedGamesDao().getGames(steam64, appIds)
    }

    override suspend fun markOwnedGameAsHidden(steam64: Long, gameId: Int) {
        db.ownedGamesDao().markGameAsHidden(steam64, gameId)
    }

    override suspend fun isUserHasGames(steam64: Long): Boolean {
        return db.ownedGamesDao().isUserHasOwnedGames(steam64)
    }

    override suspend fun clearOwnedGames(steam64: Long) {
        db.ownedGamesDao().clearGames(steam64)
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500
    }
}
