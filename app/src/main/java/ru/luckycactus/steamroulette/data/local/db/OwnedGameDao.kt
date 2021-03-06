package ru.luckycactus.steamroulette.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameMetaData
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

@Dao
abstract class OwnedGameDao : BaseDao<OwnedGameRoomEntity>() {

    private val nonAlphanumericRegex = "[^\\w]".toRegex()

    suspend fun getIds(steam64: Long, filter: GamesFilter, orderById: Boolean): List<Int> =
        getIdsMutable(steam64, filter, orderById)

    suspend fun getIdsMutable(
        steam64: Long,
        filter: GamesFilter,
        orderById: Boolean
    ): MutableList<Int> {
        val rawQuery = buildGamesRawQuery(
            "SELECT appId FROM owned_game",
            steam64,
            filter
        ) { sb, _ ->
            if (orderById) {
                sb.append(" ORDER BY appId ASC")
            }
        }
        return _getIds(rawQuery)
    }

    @Transaction
    @RawQuery
    abstract suspend fun _getIds(query: SupportSQLiteQuery): MutableList<Int>

    @Transaction
    @Query("SELECT * FROM owned_game WHERE userSteam64 = :steam64")
    abstract suspend fun getAll(steam64: Long): List<OwnedGameEntity>

    fun getLibraryPagingSource(
        steam64: Long,
        filter: GamesFilter,
        nameSearchQuery: String? = null
    ): PagingSource<Int, LibraryGame> {
        val query = makePagingSourceRawQuery(
            "SELECT appId, name, hidden FROM owned_game",
            steam64,
            filter,
            nameSearchQuery
        )
        return _getLibraryPagingSource(query)
    }

    private fun makePagingSourceRawQuery(
        query: String,
        steam64: Long,
        filter: GamesFilter,
        nameSearchQuery: String?
    ): SimpleSQLiteQuery = buildGamesRawQuery(query, steam64, filter) { sb, args ->
        if (!nameSearchQuery.isNullOrBlank()) {
            val words = nameSearchQuery.split(nonAlphanumericRegex)
            words.forEach {
                sb.append(" AND name LIKE ?")
                args += "%${it}%"
            }
        }
        sb.append(" ORDER BY name ASC")
    }

    @Transaction
    @RawQuery(observedEntities = [OwnedGameRoomEntity::class])
    abstract fun _getLibraryPagingSource(query: SupportSQLiteQuery): PagingSource<Int, LibraryGame>

    @Query(
        """SELECT appId, name
        FROM owned_game 
        WHERE appId = :gameId AND userSteam64 = :steam64"""
    )
    abstract suspend fun getHeader(steam64: Long, gameId: Int): GameHeader

    @Query(
        """SELECT appId, name
        FROM owned_game 
        WHERE appId IN (:appIds) AND userSteam64 = :steam64"""
    )
    abstract suspend fun getHeaders(steam64: Long, appIds: List<Int>): List<GameHeader>

    @Query(
        """SELECT hidden
        FROM owned_game 
        WHERE appId =:appId AND userSteam64 = :steam64"""
    )
    abstract suspend fun getHiddenState(steam64: Long, appId: Long): Boolean

    @Query(
        """SELECT appId, hidden, shown
        FROM owned_game 
        WHERE userSteam64 = :steam64"""
    )
    abstract suspend fun getAllMetaData(steam64: Long): List<OwnedGameMetaData>

    @Query("DELETE FROM owned_game WHERE userSteam64 = :steam64")
    abstract suspend fun clear(steam64: Long)

    @Query("DELETE FROM owned_game")
    abstract suspend fun clear()

    @Query(
        """UPDATE owned_game 
        SET hidden = :hidden 
        WHERE userSteam64 =:steam64 
            AND appId IN (:gameIds)"""
    )
    abstract suspend fun setHidden(steam64: Long, gameIds: List<Int>, hidden: Boolean)

    @Query(
        """UPDATE owned_game 
        SET hidden = :hidden 
        WHERE userSteam64 =:steam64"""
    )
    abstract suspend fun setAllHidden(steam64: Long, hidden: Boolean)

    @Query(
        """UPDATE owned_game 
        SET shown = :shown
        WHERE userSteam64 =:steam64 
            AND appId IN (:gameIds)"""
    )
    abstract suspend fun setShown(steam64: Long, gameIds: List<Int>, shown: Boolean)

    @Query(
        """UPDATE owned_game 
        SET shown = :shown 
        WHERE userSteam64 =:steam64"""
    )
    abstract suspend fun setAllShown(steam64: Long, shown: Boolean)

    suspend fun isUserHasGames(steam64: Long) = _isUserHasGames(steam64) == 1

    @Query(
        """SELECT COUNT(*) 
        FROM (
            SELECT appId 
            FROM owned_game 
            WHERE userSteam64 = :steam64 
            LIMIT 1
        )"""
    )
    abstract suspend fun _isUserHasGames(steam64: Long): Int

    fun observeCount(steam64: Long, filter: GamesFilter): Flow<Int> {
        val query = buildGamesRawQuery(
            "SELECT COUNT(*) FROM owned_game",
            steam64,
            filter
        )
        return _observeCount(query)
    }

    @Transaction
    @RawQuery(observedEntities = [OwnedGameRoomEntity::class])
    abstract fun _observeCount(query: SupportSQLiteQuery): Flow<Int>

    @Query(
        """UPDATE owned_game
        SET hidden = 'false'
        WHERE userSteam64 =:steam64 
            AND hidden = 'true' """
    )
    abstract suspend fun resetAllHidden(steam64: Long)

    private fun buildGamesRawQuery(
        query: String,
        steam64: Long,
        filter: GamesFilter,
        block: (StringBuilder, MutableList<Any>) -> Unit = { _, _ -> }
    ): SimpleSQLiteQuery {
        val args = mutableListOf<Any>()
        val querySb = StringBuilder()

        querySb.append(query)

        querySb.append(" WHERE userSteam64 = ?")
        args += steam64

        filter.shown?.let {
            args += it
            querySb.append(" AND shown = ?")
        }

        filter.hidden?.let {
            args += it
            querySb.append(" AND hidden = ?")
        }

        val maxHours = when (filter.playtime) {
            PlaytimeFilter.All -> null
            PlaytimeFilter.NotPlayed -> 0
            is PlaytimeFilter.Limited -> filter.playtime.maxHours
        }

        maxHours?.let {
            args += it * 60
            querySb.append(" AND playtimeForever <= ?")
        }

        block(querySb, args)
        return SimpleSQLiteQuery(querySb.toString(), args.toTypedArray())
    }
}