package ru.luckycactus.steamroulette.data.repositories.games.datastore

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.intellij.lang.annotations.Language
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.local.db.DB
import ru.luckycactus.steamroulette.data.repositories.games.mapper.OwnedGameRoomEntityMapper
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.common.chunkBuffer
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.presentation.utils.longLog
import ru.luckycactus.steamroulette.presentation.utils.onDebug
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

            val logger = GamesParseLogger(false)
            gamesFlow
                .filter { game ->
                    val suspicious = game.iconUrl.isNullOrEmpty() || game.logoUrl.isNullOrEmpty()
                    if (gameIds.contains(game.appId)) {
                        onDebug {
                            if (suspicious)
                                logger.addSuspiciousGame(game)
                        }
                        true
                    } else {
                        var banned = game.iconUrl.isNullOrEmpty() && game.logoUrl.isNullOrEmpty()
                        if (!banned && !game.name.isNullOrEmpty()) {
                            banned = banRegex.containsMatchIn(game.name)

                            if (!banned && suspicious) {
                                banned = banIfSuspiciousRegex.containsMatchIn(game.name)
                            }
                        }
                        if (banned) {
                            logger.addBannedGame(game)
                        } else if (suspicious) {
                            logger.addSuspiciousGame(game)
                        }
                        !banned
                    }
                }
                .map { mapper.mapFrom(it) }
                .chunkBuffer(GAMES_BUFFER_SIZE)
                .collect {
                    db.ownedGamesDao().insert(it)
                }
            logger.log()
        }
    }

    override suspend fun getOwnedGamesIds(
        steamId: SteamId,
        filter: PlaytimeFilter
    ): List<Int> = db.ownedGamesDao().run {
        when (filter) {
            PlaytimeFilter.All -> getVisibleIds(steamId.asSteam64())
            PlaytimeFilter.NotPlayed -> getVisibleLimitedByPlaytimeIds(steamId.asSteam64(), 0)
            is PlaytimeFilter.Limited -> getVisibleLimitedByPlaytimeIds(
                steamId.asSteam64(),
                filter.maxTime
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

    override suspend fun hideOwnedGame(steamId: SteamId, gameId: Int) {
        db.ownedGamesDao().hide(steamId.asSteam64(), gameId)
    }

    override suspend fun isUserHasGames(steamId: SteamId): Boolean =
        db.ownedGamesDao().isUserHasGames(steamId.asSteam64())

    override suspend fun clearOwnedGames(steamId: SteamId) {
        db.ownedGamesDao().delete(steamId.asSteam64())
    }

    private class GamesParseLogger(
        private val enable: Boolean = BuildConfig.DEBUG
    ) {
        private val bannedGames = if (enable) {
            mutableListOf<String>()
        } else null
        private val suspiciousGames = if (enable) {
            mutableListOf<String>()
        } else null

        fun addBannedGame(game: OwnedGameEntity) {
            bannedGames?.add(game.name!!)
        }

        fun addSuspiciousGame(game: OwnedGameEntity) {
            suspiciousGames?.add(game.name!!)
        }

        fun log() {
            if (enable) {
                longLog(
                    "GamesParseLogger",
                    "Banned games (${bannedGames!!.size}): ${bannedGames.joinToString(separator = "\n")}"
                )
                longLog(
                    "GamesParseLogger",
                    "Suspicious games (${suspiciousGames!!.size}): ${suspiciousGames.joinToString(
                        separator = "\n"
                    )}"
                )
            }
        }
    }

    companion object {
        private const val GAMES_BUFFER_SIZE = 500

        @Language("RegExp")
        private val banRegex = arrayOf(
            "public test$",
            "public testing$",
            "closed test$",
            "system test$",
            "test server$",
            "testlive client$",
            "screen tests$",
            " demo$",
            " beta$",
            "\\(Theatrical",
            "\\(Subtitled"
        ).joinToString(separator = "|")
            .toRegex(RegexOption.IGNORE_CASE)

        @Language("RegExp")
        private val banIfSuspiciousRegex =
            arrayOf(
                "\\btest$",
                "\\bEp\\d\\d",
                "Player Profiles",
                "\\bSkin\\b",
                "\\(Class\\)"

            ).joinToString(separator = "|")
                .toRegex(RegexOption.IGNORE_CASE)
    }
}
