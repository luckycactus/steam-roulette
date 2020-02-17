package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.lifecycle.LiveData
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
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.common.chunkBuffer
import javax.inject.Inject

@Reusable
class LocalGamesDataStore @Inject constructor(
    private val db: DB
) : GamesDataStore.Local {

    override fun observeOwnedGamesCount(steamId: SteamId): LiveData<Int> =
        db.ownedGamesDao().observeCount(steamId.asSteam64())

    override fun observeHiddenOwnedGamesCount(steamId: SteamId): LiveData<Int> =
        db.ownedGamesDao().observeHiddenCount(steamId.asSteam64())

    override suspend fun saveOwnedGames(steamId: SteamId, gamesFlow: Flow<OwnedGameEntity>) {
        db.withTransaction {
            val gameIds = db.ownedGamesDao().getAllIds(steamId.asSteam64()).toSet()
            val hiddenGameIds = db.ownedGamesDao().getHiddenIds(steamId.asSteam64()).toSet()
            val mapper = OwnedGameRoomEntityMapper(steamId.asSteam64(), hiddenGameIds)

            db.ownedGamesDao().deleteAll(steamId.asSteam64())

            gamesFlow
                .filter { game ->
                    if (gameIds.contains(game.appId)) {
                        true
                    } else {
                        var banned = game.iconUrl.isNullOrEmpty() && game.logoUrl.isNullOrEmpty()
                        if (!banned && !game.name.isNullOrEmpty()) {
                            banned = bannedEndings.any { game.name.endsWith(it, true) }
                        }
                        !banned
                    }
                }
                .map { mapper.mapFrom(it) }
                .chunkBuffer(GAMES_BUFFER_SIZE)
                .collect {
                    db.ownedGamesDao().insert(it)
                }
        }
    }

    override suspend fun getOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter
    ): List<Int> = db.ownedGamesDao().run {
        when (filter) {
            PlaytimeFilter.All -> getVisibleIds(steamId.asSteam64())
            PlaytimeFilter.NotPlayed -> getVisibleLimitedByPlaytimeIds(steamId.asSteam64(), 0)
            is PlaytimeFilter.Limited -> getVisibleLimitedByPlaytimeIds(steamId.asSteam64(), filter.maxTime)
        }
    }

    override suspend fun resetHiddenOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().resetHidden(steamId.asSteam64())
    }

    override suspend fun getOwnedGameHeader(steamId: SteamId, gameId: Int): GameHeader =
        db.ownedGamesDao().getHeader(steamId.asSteam64(), gameId)

    override suspend fun getOwnedGameHeaders(steamId: SteamId, gameIds: List<Int>): List<GameHeader> =
        db.ownedGamesDao().getHeaders(steamId.asSteam64(), gameIds)

    override suspend fun hideOwnedGame(steamId: SteamId, gameId: Int) {
        db.ownedGamesDao().hide(steamId.asSteam64(), gameId)
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        db.ownedGamesDao().isUserHasGames(steamId.asSteam64())

    override suspend fun clearOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().delete(steamId.asSteam64())
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500
        private val bannedEndings = arrayOf(
            "public test",
            "public testing",
            "closed test",
            "system test",
            "test server",
            "testlive client",
            "screen tests",

            " demo",
            " beta"
        )
    }
}
