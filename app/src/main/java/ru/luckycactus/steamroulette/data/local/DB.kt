package ru.luckycactus.steamroulette.data.local

import androidx.room.*
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import androidx.lifecycle.LiveData



@Database(
    entities = [OwnedGameRoomEntity::class],
    version = 4
)
abstract class DB : RoomDatabase() {

    abstract fun ownedGamesDao(): OwnedGamesDao
}

@Dao
abstract class OwnedGamesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertGames(games: List<OwnedGameRoomEntity>)

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where userSteam64 = :steam64"""
    )
    abstract suspend fun getGames(steam64: Long): List<OwnedGame>

    @Query("""SELECT appId from owned_game where userSteam64 = :steam64 and hidden = 0""")
    abstract suspend fun getVisibleGamesIdList(steam64: Long): List<Int>

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where appId = :appId"""
    )
    abstract suspend fun getGameById(appId: Int): OwnedGame

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where appId = :gameId and userSteam64 = :steam64"""
    )
    abstract suspend fun getGame(steam64: Long, gameId: Int): OwnedGame

    @Query("UPDATE owned_game SET hidden = 1 WHERE userSteam64 =:steam64 and appId = :gameId")
    abstract suspend fun markGameAsHidden(steam64: Long, gameId: Int)

    @Query("select appId from owned_game where userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun getHiddenGamesIds(steam64: Long): List<Int>

    @Query("delete from owned_game where userSteam64 =:steam64 and updateTimeStamp < :timestamp")
    abstract suspend fun removeGamesUpdatedEarlierThen(steam64: Long, timestamp: Long)

    @Transaction
    open suspend fun insertGamesRemoveOthers(steam64: Long, timestamp: Long, games: List<OwnedGameRoomEntity>) {
        insertGames(games)
        removeGamesUpdatedEarlierThen(steam64, timestamp)
    }

    suspend fun isUserHasOwnedGames(steam64: Long) = _isUserHasOwnedGames(steam64) == 1

    @Query("select count(*) from owned_game where userSteam64 = :steam64")
    abstract suspend fun getGamesCount(steam64: Long): Int

    @Query("SELECT COUNT(*) FROM owned_game  where userSteam64 = :steam64")
    abstract fun observeGameCount(steam64: Long): LiveData<Int>


    @Query("select COUNT(*) from (select appId from owned_game where userSteam64 = :steam64 limit 1)")
    abstract suspend fun _isUserHasOwnedGames(steam64: Long): Int
}