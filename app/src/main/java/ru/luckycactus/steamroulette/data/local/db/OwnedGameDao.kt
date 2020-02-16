package ru.luckycactus.steamroulette.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame

@Dao
abstract class OwnedGameDao : BaseDao<OwnedGameRoomEntity>() {

    @Query("select appId from owned_game where userSteam64 = :steam64 and hidden = 0")
    abstract suspend fun getVisibleIds(steam64: Long): List<Int>

    @Query("select appId from owned_game where userSteam64 = :steam64")
    abstract suspend fun getAllIds(steam64: Long): List<Int>

    @Query("select appId from owned_game where userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun getHiddenIds(steam64: Long): List<Int>

    @Query(
        """select appId 
        from owned_game 
        where userSteam64 = :steam64 and hidden = 0 and playtimeForever <= :maxHours * 60"""
    )
    abstract suspend fun getVisibleLimitedByPlaytimeIds(steam64: Long, maxHours: Int): List<Int>

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
        from owned_game 
        where appId = :gameId and userSteam64 = :steam64"""
    )
    abstract suspend fun get(steam64: Long, gameId: Int): OwnedGame

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
        from owned_game 
        where appId in (:appIds) and userSteam64 = :steam64"""
    )
    abstract suspend fun get(steam64: Long, appIds: List<Int>): List<OwnedGame>

    @Query("delete from owned_game where userSteam64 = :steam64")
    abstract suspend fun delete(steam64: Long)

    @Query("update owned_game SET hidden = 1 where userSteam64 =:steam64 and appId = :gameId")
    abstract suspend fun hide(steam64: Long, gameId: Int)

    @Query("delete from owned_game where userSteam64 = :steam64")
    abstract suspend fun deleteAll(steam64: Long)

    suspend fun isUserHasGames(steam64: Long) = _isUserHasGames(steam64) == 1

    @Query("select COUNT(*) from owned_game  where userSteam64 = :steam64")
    abstract fun observeCount(steam64: Long): LiveData<Int>

    @Query("select COUNT(*) from owned_game  where userSteam64 = :steam64 and hidden=1")
    abstract fun observeHiddenCount(steam64: Long): LiveData<Int>

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