package ru.luckycactus.steamroulette.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

@Database(
    entities = [OwnedGameRoomEntity::class, UserSummaryEntity::class],
    version = 2
)
abstract class DB : RoomDatabase() {

    abstract fun ownedGamesDao(): OwnedGameDao

    abstract fun userSummaryDao(): UserSummaryDao
}