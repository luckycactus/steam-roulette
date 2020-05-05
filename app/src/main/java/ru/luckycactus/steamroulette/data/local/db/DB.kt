package ru.luckycactus.steamroulette.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity

@Database(
    entities = [OwnedGameRoomEntity::class, UserSummaryEntity::class],
    version = 3
)
abstract class DB : RoomDatabase() {

    abstract fun ownedGamesDao(): OwnedGameDao

    abstract fun userSummaryDao(): UserSummaryDao
}