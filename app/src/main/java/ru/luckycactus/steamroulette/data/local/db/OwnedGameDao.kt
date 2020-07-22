package ru.luckycactus.steamroulette.data.local.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameAppData
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader

@Dao
abstract class OwnedGameDao : BaseDao<OwnedGameRoomEntity>() {

    @Query("select appId from owned_game where userSteam64 = :steam64 and hidden = 0 and shown = :shown")
    abstract suspend fun getVisibleIds(steam64: Long, shown: Boolean): List<Int>

    @Query(
        """select appId 
        from owned_game 
        where userSteam64 = :steam64 and hidden = 0  and shown = :shown and playtimeForever <= :maxHours * 60"""
    )
    abstract suspend fun getVisibleLimitedByPlaytimeIds(
        steam64: Long,
        maxHours: Int,
        shown: Boolean
    ): List<Int>

    @Transaction
    @Query("select * from owned_game where userSteam64 = :steam64")
    abstract suspend fun getAll(steam64: Long): List<OwnedGameEntity>

    @Query("select appId from owned_game where userSteam64 = :steam64")
    abstract suspend fun getAllIds(steam64: Long): List<Int>

    @Query("select appId from owned_game where userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun getHiddenIds(steam64: Long): List<Int>

    @Query("select appId from owned_game where userSteam64 =:steam64 and shown = 1")
    abstract suspend fun getShownIds(steam64: Long): List<Int>

    @Query("select appId, name from owned_game where userSteam64 =:steam64 and hidden = 1 order by name asc")
    abstract fun getHiddenGamesDataSourceFactory(steam64: Long): DataSource.Factory<Int, GameHeader>

    @Query(
        """select appId, name
        from owned_game 
        where appId = :gameId and userSteam64 = :steam64"""
    )
    abstract suspend fun getHeader(steam64: Long, gameId: Int): GameHeader

    @Query(
        """select appId, name
        from owned_game 
        where appId in (:appIds) and userSteam64 = :steam64"""
    )
    abstract suspend fun getHeaders(steam64: Long, appIds: List<Int>): List<GameHeader>

    @Query(
        """select appId, hidden, shown
        from owned_game 
        where userSteam64 = :steam64"""
    )
    abstract suspend fun getAllAppData(steam64: Long): List<OwnedGameAppData>

    @Query("delete from owned_game where userSteam64 = :steam64")
    abstract suspend fun clear(steam64: Long)

    @Query("delete from owned_game")
    abstract suspend fun clear()

    @Query("update owned_game SET hidden = :hidden where userSteam64 =:steam64 and appId in (:gameIds)")
    abstract suspend fun setHidden(steam64: Long, gameIds: List<Int>, hidden: Boolean)

    @Query("update owned_game SET hidden = :hidden where userSteam64 =:steam64")
    abstract suspend fun setAllHidden(steam64: Long, hidden: Boolean)

    @Query("update owned_game SET shown = :shown where userSteam64 =:steam64 and appId in (:gameIds)")
    abstract suspend fun setShown(steam64: Long, gameIds: List<Int>, shown: Boolean)

    @Query("update owned_game SET shown = :shown where userSteam64 =:steam64")
    abstract suspend fun setAllShown(steam64: Long, shown: Boolean)

    suspend fun isUserHasGames(steam64: Long) = _isUserHasGames(steam64) == 1

    @Query("select COUNT(*) from owned_game  where userSteam64 = :steam64")
    abstract fun observeCount(steam64: Long): Flow<Int>

    @Query("select COUNT(*) from owned_game  where userSteam64 = :steam64 and hidden = 1")
    abstract fun observeHiddenCount(steam64: Long): Flow<Int>

    @Query("update owned_game set hidden = 0 where userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun resetHidden(steam64: Long)

    @Query(
        """select COUNT(*) 
        from (
            select appId 
            from owned_game 
            where userSteam64 = :steam64 
            limit 1
        )"""
    )
    abstract suspend fun _isUserHasGames(steam64: Long): Int
}