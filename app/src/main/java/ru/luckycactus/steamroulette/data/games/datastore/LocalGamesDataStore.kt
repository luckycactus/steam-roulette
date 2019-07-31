package ru.luckycactus.steamroulette.data.games.datastore

import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

class LocalGamesDataStore(
    private val db: DB,
    private val gameRoomEntityMapperFactory: OwnedGameRoomEntityMapper.Factory
) : GamesDataStore.Local {
    override suspend fun getOwnedGames(steam64: Long): List<OwnedGame> {
        return db.ownedGamesDao().getGames(steam64)

    }

    override suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>) {
        val mapper = gameRoomEntityMapperFactory.create(steam64)
        db.ownedGamesDao().insertGames(mapper.mapFrom(games))
    }

    override suspend fun getOwnedGamesNumbers(steam64: Long): List<Int> {
        return db.ownedGamesDao().getGamesRowIdList(steam64)

    }

    override suspend fun getOwnedGameByNumber(number: Int): OwnedGame {
        return db.ownedGamesDao().getGameByRowId(number)

    }

    override suspend fun markGameAsHidden(steam64: Long, gameId: Long) {
        db.ownedGamesDao().markGameAsHidden(steam64, gameId)
    }
}
