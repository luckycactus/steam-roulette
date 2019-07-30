package ru.luckycactus.steamroulette.data.local

import androidx.room.*
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame

@Database(
    entities = [OwnedGameRoomEntity::class],
    version = 2
)
abstract class DB : RoomDatabase() {

    abstract fun ownedGamesDao(): OwnedGamesDao
}

@Dao
abstract class OwnedGamesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) //todo not replace
    abstract suspend fun insertGames(games: List<OwnedGameRoomEntity>)

    @Query(
        """select appId, name, playtime2Weeks, playtimeForever, iconUrl, logoUrl 
            from owned_game 
            where userSteam64 = :steam64"""
    )
    abstract suspend fun getGames(steam64: Long): List<OwnedGame>

    @Query("""SELECT _rowid_ from owned_game where userSteam64 = :steam64""")
    abstract suspend fun getGamesRowIdList(steam64: Long): List<Int>

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
}