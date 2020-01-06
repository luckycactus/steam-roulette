package ru.luckycactus.steamroulette.data.games.datastore

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.utils.chunkBuffer
import javax.inject.Inject

@Reusable
class LocalGamesDataStore @Inject constructor(
    private val db: DB
) : GamesDataStore.Local {

    override fun observeOwnedGamesCount(steam64: Long): LiveData<Int> {
        return db.ownedGamesDao().observeCount(steam64)
    }

    override fun observeHiddenOwnedGamesCount(steam64: Long): LiveData<Int> {
        return db.ownedGamesDao().observeHiddenCount(steam64)
    }

    override suspend fun saveOwnedGames(steam64: Long, gamesFlow: Flow<OwnedGameEntity>) {
        db.withTransaction {
            val hiddenGameIds = db.ownedGamesDao().getHiddenIds(steam64).toSet()
            val mapper = OwnedGameRoomEntityMapper(steam64, hiddenGameIds)

            db.ownedGamesDao().deleteAll(steam64)

            gamesFlow
                .map { mapper.mapFrom(it) }
                .chunkBuffer(GAMES_BUFFER_SIZE)
                .collect {
                    db.ownedGamesDao().insert(it)
                }
        }
    }

    override suspend fun getFilteredOwnedGamesIds(
        steam64: Long,
        filter: PlaytimeFilter
    ): List<Int> {
        return db.ownedGamesDao().run {
            when (filter) {
                PlaytimeFilter.All -> getVisibleIds(steam64)
                PlaytimeFilter.NotPlayed -> getVisibleLimitedByPlaytimeIds(steam64, 0)
                is PlaytimeFilter.Limited -> getVisibleLimitedByPlaytimeIds(steam64, filter.maxTime)
            }
        }
    }

    override suspend fun clearHiddenOwnedGames(steam64: Long) {
        db.ownedGamesDao().clearHidden(steam64)
    }

    override suspend fun getOwnedGame(steam64: Long, gameId: Int): OwnedGame {
        return db.ownedGamesDao().get(steam64, gameId)
    }

    override suspend fun getOwnedGames(steam64: Long, gameIds: List<Int>): List<OwnedGame> {
        return db.ownedGamesDao().get(steam64, gameIds)
    }

    override suspend fun hideOwnedGame(steam64: Long, gameId: Int) {
        db.ownedGamesDao().hide(steam64, gameId)
    }

    override suspend fun isUserHasGames(steam64: Long): Boolean {
        return db.ownedGamesDao().isUserHasGames(steam64)
    }

    override suspend fun clearOwnedGames(steam64: Long) {
        db.ownedGamesDao().delete(steam64)
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500
    }
}
