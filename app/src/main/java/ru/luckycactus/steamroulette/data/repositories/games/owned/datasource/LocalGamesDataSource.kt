package ru.luckycactus.steamroulette.data.repositories.games.owned.datasource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.repositories.games.owned.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.chunkBuffer
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

@Reusable
class LocalGamesDataSource @Inject constructor(
    private val db: AppDatabase,
    private val gamesValidatorFactory: GamesValidator.Factory
) : GamesDataSource.Local {

    override fun observeCount(steamId: SteamId, filter: GamesFilter): Flow<Int> =
        db.ownedGamesDao().observeCount(steamId.as64(), filter)

    override suspend fun update(
        steamId: SteamId,
        gamesFlow: Flow<OwnedGameEntity>
    ) {
        val steam64 = steamId.as64()
        db.withTransaction {
            val metaData = db.ownedGamesDao().getAllMetaData(steam64).associateBy { it.appId }
            val mapper = OwnedGameRoomEntityMapper(steam64, metaData)
            val gamesVerifier = gamesValidatorFactory.create(metaData.keys)

            db.ownedGamesDao().clear(steam64)

            gamesFlow
                .filter { gamesVerifier.validate(it) }
                .map { mapper.mapFrom(it) }
                .chunkBuffer(GAMES_BUFFER_SIZE)
                .collect {
                    db.ownedGamesDao().insert(it)
                }
            gamesVerifier.log()
        }
    }

    override suspend fun getAll(steamId: SteamId): List<OwnedGameEntity> {
        return db.ownedGamesDao().getAll(steamId.as64())
    }

    override suspend fun getIds(
        steamId: SteamId,
        filter: GamesFilter,
        orderById: Boolean
    ): List<Int> = db.ownedGamesDao().getIds(steamId.as64(), filter, orderById)

    override suspend fun getIdsMutable(
        steamId: SteamId,
        filter: GamesFilter,
        orderById: Boolean
    ): MutableList<Int> = db.ownedGamesDao().getIdsMutable(steamId.as64(), filter, orderById)

    override suspend fun resetAllHidden(steamId: SteamId) {
        db.ownedGamesDao().resetAllHidden(steamId.as64())
    }

    override suspend fun getHeader(steamId: SteamId, gameId: Int): GameHeader =
        db.ownedGamesDao().getHeader(steamId.as64(), gameId)

    override suspend fun getHeaders(
        steamId: SteamId,
        gameIds: List<Int>
    ): List<GameHeader> = db.ownedGamesDao().getHeaders(steamId.as64(), gameIds)

    override suspend fun setHidden(steamId: SteamId, gameIds: List<Int>, hide: Boolean) {
        db.ownedGamesDao().setHidden(steamId.as64(), gameIds, hide)
    }

    override suspend fun setAllHidden(steamId: SteamId, hide: Boolean) {
        db.ownedGamesDao().setAllHidden(steamId.as64(), hide)
    }

    override suspend fun setShown(steamId: SteamId, gameIds: List<Int>, shown: Boolean) {
        db.ownedGamesDao().setShown(steamId.as64(), gameIds, shown)
    }

    override suspend fun setAllShown(steamId: SteamId, shown: Boolean) {
        db.ownedGamesDao().setAllShown(steamId.as64(), shown)
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        db.ownedGamesDao().isUserHasGames(steamId.as64())

    override suspend fun clear(steamId: SteamId) {
        db.ownedGamesDao().clear(steamId.as64())
    }

    override fun getLibraryPagingSource(
        steamId: SteamId,
        filter: GamesFilter,
        nameSearchQuery: String?
    ): PagingSource<Int, LibraryGame> = db.ownedGamesDao().getLibraryPagingSource(
        steamId.as64(),
        filter,
        nameSearchQuery
    )

    override suspend fun getHiddenState(steamId: SteamId, appId: Long): Boolean {
        return db.ownedGamesDao().getHiddenState(steamId.as64(), appId)
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500
    }
}
