package ru.luckycactus.steamroulette.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

@Dao
abstract class OwnedGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertGames(games: List<OwnedGameRoomEntity>)

    @Query("""SELECT appId from owned_game where userSteam64 = :steam64 and hidden = 0""")
    abstract suspend fun getVisibleGamesIdList(steam64: Long): List<Int>

    @Query("""SELECT appId from owned_game where userSteam64 = :steam64 and hidden = 0 and playtime2Weeks = 0""")
    abstract suspend fun getVisibleNotPlayed2WeeksGamesIdList(steam64: Long): List<Int>

    @Query("""SELECT appId from owned_game where userSteam64 = :steam64 and hidden = 0 and playtimeForever = 0""")
    abstract suspend fun getVisibleNotPlayedGamesIdList(steam64: Long): List<Int>

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where appId = :gameId and userSteam64 = :steam64"""
    )
    abstract suspend fun getGame(steam64: Long, gameId: Int): OwnedGame

    @Query("UPDATE owned_game SET hidden = 1 WHERE userSteam64 =:steam64 and appId = :gameId")
    abstract suspend fun hideGame(steam64: Long, gameId: Int)

    @Query("select appId from owned_game where userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun getHiddenGamesIds(steam64: Long): List<Int>

    @Query("delete from owned_game where userSteam64 =:steam64 and updateTimeStamp < :timestamp")
    abstract suspend fun removeGamesUpdatedEarlierThen(steam64: Long, timestamp: Long)

    suspend fun isUserHasGames(steam64: Long) = _isUserHasOwnedGames(steam64) == 1

    @Query("SELECT COUNT(*) FROM owned_game  where userSteam64 = :steam64")
    abstract fun observeGameCount(steam64: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM owned_game  where userSteam64 = :steam64 and hidden=1")
    abstract fun observeHiddenGameCount(steam64: Long): LiveData<Int>

    @Query("UPDATE owned_game SET hidden = 0 WHERE userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun clearHiddenGames(steam64: Long)

    @Query("select COUNT(*) from (select appId from owned_game where userSteam64 = :steam64 limit 1)")
    abstract suspend fun _isUserHasOwnedGames(steam64: Long): Int

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where appId in (:appIds) and userSteam64 = :steam64"""
    )
    abstract suspend fun getGames(steam64: Long, appIds: List<Int>): List<OwnedGame>

    @Query("""delete from owned_game where userSteam64 = :steam64""")
    abstract suspend fun clearGames(steam64: Long)
}