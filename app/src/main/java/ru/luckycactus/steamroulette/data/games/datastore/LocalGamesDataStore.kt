package ru.luckycactus.steamroulette.data.games.datastore

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.EnPlayTimeFilter
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

class LocalGamesDataStore(
    private val db: DB
) : GamesDataStore.Local {
    override suspend fun getOwnedGames(steam64: Long): List<OwnedGame> {
        return db.ownedGamesDao().getGames(steam64)
    }

    override fun observeGameCount(steam64: Long): LiveData<Int> {
        return db.ownedGamesDao().observeGameCount(steam64)
    }

    override fun observeHiddenGameCount(steam64: Long): LiveData<Int> {
        return db.ownedGamesDao().observeHiddenGameCount(steam64)
    }

    override suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>) {
        val hiddenGameIds = db.ownedGamesDao().getHiddenGamesIds(steam64).toSet()
        val timestamp = System.currentTimeMillis()
        val mapper = OwnedGameRoomEntityMapper(steam64, hiddenGameIds, timestamp)
        db.ownedGamesDao().insertGamesRemoveOthers(steam64, timestamp, mapper.mapFrom(games))
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

    override suspend fun clearHiddenGames(steam64: Long) {
        db.ownedGamesDao().clearHiddenGames(steam64)
    }

    override suspend fun getOwnedGame(steam64: Long, appId: Int): OwnedGame {
        return db.ownedGamesDao().getGame(steam64, appId)
    }

    override suspend fun getOwnedGames(steam64: Long, appIds: List<Int>): List<OwnedGame> {
        return db.ownedGamesDao().getGames(steam64, appIds)
    }

    override suspend fun markGameAsHidden(steam64: Long, gameId: Int) {
        db.ownedGamesDao().markGameAsHidden(steam64, gameId)
    }

    override suspend fun isUserHasOwnedGames(steam64: Long): Boolean {
        return db.ownedGamesDao().isUserHasOwnedGames(steam64)
    }

    override suspend fun clearGames(steam64: Long) {
        db.ownedGamesDao().clearGames(steam64)
    }
}
