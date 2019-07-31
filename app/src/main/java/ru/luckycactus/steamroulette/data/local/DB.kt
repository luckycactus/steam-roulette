package ru.luckycactus.steamroulette.data.local

import androidx.room.*
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

@Database(
    entities = [OwnedGameRoomEntity::class],
    version = 3
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

    @Query("""SELECT _rowid_ from owned_game where userSteam64 = :steam64 and hidden = 0""")
    abstract suspend fun getVisibleGamesRowIdList(steam64: Long): List<Int>

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where _rowid_ = :rowId"""
    )
    abstract suspend fun getGameByRowId(rowId: Int): OwnedGame

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where appId = :gameId and userSteam64 = :steam64"""
    )
    abstract suspend fun getGame(steam64: Long, gameId: Long): OwnedGame

    @Query("UPDATE owned_game SET hidden = 1 WHERE userSteam64 =:steam64 and appId = :gameId")
    abstract suspend fun markGameAsHidden(steam64: Long, gameId: Long)

    @Query("select appId from owned_game where userSteam64 =:steam64 and hidden = 1")
    abstract suspend fun getHiddenGamesIds(steam64: Long): List<Long>

    @Query("delete from owned_game where userSteam64 =:steam64 and updateTimeStamp < :timestamp")
    abstract suspend fun removeGamesUpdatedEarlierThen(steam64: Long, timestamp: Long)

    @Transaction
    open suspend fun insertGamesRemoveOthers(steam64: Long, timestamp: Long, games: List<OwnedGameRoomEntity>) {
        insertGames(games)
        removeGamesUpdatedEarlierThen(steam64, timestamp)
    } }