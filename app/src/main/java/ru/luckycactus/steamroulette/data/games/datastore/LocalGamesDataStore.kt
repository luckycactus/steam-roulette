package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

class LocalGamesDataStore(
    private val db: DB
) : GamesDataStore.Local {
    override suspend fun getOwnedGames(steam64: Long): List<OwnedGame> {
        return db.ownedGamesDao().getGames(steam64)
    }

    override suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>) {
        val hiddenGameIds = db.ownedGamesDao().getHiddenGamesIds(steam64).toSet()
        val timestamp = System.currentTimeMillis()
        val mapper = OwnedGameRoomEntityMapper(steam64, hiddenGameIds, timestamp)
        db.ownedGamesDao().insertGamesRemoveOthers(steam64, timestamp, mapper.mapFrom(games))
    }

    override suspend fun getOwnedGamesNumbers(steam64: Long): List<Int> {
        return db.ownedGamesDao().getVisibleGamesRowIdList(steam64)
    }

    override suspend fun getOwnedGameByNumber(number: Int): OwnedGame {
        return db.ownedGamesDao().getGameByRowId(number)

    }

    override suspend fun markGameAsHidden(steam64: Long, gameId: Long) {
        db.ownedGamesDao().markGameAsHidden(steam64, gameId)
    }
}
