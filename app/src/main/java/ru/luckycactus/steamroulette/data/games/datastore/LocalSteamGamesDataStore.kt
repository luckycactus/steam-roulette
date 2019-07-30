package ru.luckycactus.steamroulette.data.games.datastore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

class LocalSteamGamesDataStore(
    private val db: DB,
    private val gameRoomEntityMapperFactory: OwnedGameRoomEntityMapper.Factory
) : SteamGamesDataStore.Local {
    override suspend fun getOwnedGames(steam64: Long): List<OwnedGame> {
        return withContext(Dispatchers.IO) {
            db.ownedGamesDao().getGames(steam64)
        }
    }

    override suspend fun saveOwnedGamesToCache(steam64: Long, games: List<OwnedGameEntity>) {
        val mapper = gameRoomEntityMapperFactory.create(steam64)
        withContext(Dispatchers.IO) {
            db.ownedGamesDao().insertGames(mapper.mapFrom(games))
        }
    }

    override suspend fun getOwnedGamesNumbers(steam64: Long): List<Int> {
        return withContext(Dispatchers.IO) {
            db.ownedGamesDao().getGamesRowIdList(steam64)
        }
    }

    override suspend fun getOwnedGameByNumber(number: Int): OwnedGame {
        return withContext(Dispatchers.IO) {
            db.ownedGamesDao().getGameByRowId(number)
        }
    }

    override suspend fun markGameAsHidden(steam64: Long, gameId: Long) {
        withContext(Dispatchers.IO) {
            db.ownedGamesDao().markGameAsHidden(steam64, gameId)
        }
    }
}
