package ru.luckycactus.steamroulette.data.local.db

import android.util.Log
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

@Dao
abstract class OwnedGameDao : BaseDao<OwnedGameRoomEntity>() {

    private val nonAlphanumericRegex = "[^\\w]".toRegex()

    suspend fun getIds(
        steam64: Long,
        shown: Boolean? = null,
        hidden: Boolean? = null,
        maxHours: Int? = null
    ): List<Int> {
        val querySb = StringBuilder("SELECT appId FROM owned_game WHERE userSteam64 = ?")
        val args = mutableListOf<Any>(steam64)

        shown?.let {
            args += it
            querySb.append(" AND shown = ?")
        }
        hidden?.let {
            args += it
            querySb.append(" AND hidden = ?")
        }
        maxHours?.let {
            args += it * 60
            querySb.append(" AND playtimeForever <= ?")
        }

        return _getIds(SimpleSQLiteQuery(querySb.toString(), args.toTypedArray()))
    }

    @Transaction
    @Query("SELECT * FROM owned_game WHERE userSteam64 = :steam64")
    abstract suspend fun getAll(steam64: Long): List<OwnedGameEntity>

    fun getPagingSource(
        steam64: Long,
        shown: Boolean? = null,
        hidden: Boolean? = null,
        maxHours: Int? = null,
        nameSearchQuery: String? = null
    ): PagingSource<Int, GameHeader> {
        val querySb = StringBuilder("SELECT appId, name FROM owned_game WHERE userSteam64 = ?")
        val args = mutableListOf<Any>(steam64)

        shown?.let {
            args += it
            querySb.append(" AND shown = ?")
        }
        hidden?.let {
            args += it
            querySb.append(" AND hidden = ?")
        }
        maxHours?.let {
            args += it * 60
            querySb.append(" AND playtimeForever <= ?")
        }
        if (!nameSearchQuery.isNullOrBlank()) {
            for (word in nameSearchQuery.split(nonAlphanumericRegex)) {
                querySb.append(" AND name LIKE ?")
                args += "%${word}%"
            }
        }

        querySb.append(" ORDER BY name ASC")

        return _getGamesPagingSource(SimpleSQLiteQuery(querySb.toString(), args.toTypedArray()))
    }

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
        """SELECT appId, hidden, shown
        FROM owned_game 
        WHERE userSteam64 = :steam64"""
    )
    abstract suspend fun getAllMetaData(steam64: Long): List<OwnedGameMetaData>

    @Query("DELETE FROM owned_game WHERE userSteam64 = :steam64")
    abstract suspend fun clear(steam64: Long)

    @Query("DELETE FROM owned_game")
    abstract suspend fun clear()

    @Query("UPDATE owned_game SET hidden = :hidden WHERE userSteam64 =:steam64 AND appId IN (:gameIds)")
    abstract suspend fun setHidden(steam64: Long, gameIds: List<Int>, hidden: Boolean)

    @Query("UPDATE owned_game SET hidden = :hidden WHERE userSteam64 =:steam64")
    abstract suspend fun setAllHidden(steam64: Long, hidden: Boolean)

    @Query("UPDATE owned_game SET shown = :shown WHERE userSteam64 =:steam64 AND appId IN (:gameIds)")
    abstract suspend fun setShown(steam64: Long, gameIds: List<Int>, shown: Boolean)

    @Query("UPDATE owned_game SET shown = :shown WHERE userSteam64 =:steam64")
    abstract suspend fun setAllShown(steam64: Long, shown: Boolean)

    suspend fun isUserHasGames(steam64: Long) = _isUserHasGames(steam64) == 1

    @Query("SELECT COUNT(*) FROM owned_game  WHERE userSteam64 = :steam64")
    abstract fun observeCount(steam64: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM owned_game  WHERE userSteam64 = :steam64 AND hidden = 1")
    abstract fun observeHiddenCount(steam64: Long): Flow<Int>

    @Query("UPDATE owned_game set hidden = 0 WHERE userSteam64 =:steam64 AND hidden = 1")
    abstract suspend fun resetAllHidden(steam64: Long)

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

    @Transaction
    @RawQuery
    abstract suspend fun _getIds(query: SupportSQLiteQuery): List<Int>

    @Transaction
    @RawQuery(observedEntities = [OwnedGameRoomEntity::class])
    abstract fun _getGamesPagingSource(query: SupportSQLiteQuery): PagingSource<Int, GameHeader>
}