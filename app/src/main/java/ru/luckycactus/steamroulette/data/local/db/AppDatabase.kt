package ru.luckycactus.steamroulette.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity

@Database(
    entities = [OwnedGameRoomEntity::class, UserSummaryEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ownedGamesDao(): OwnedGameDao
    abstract fun userSummaryDao(): UserSummaryDao

    companion object {
        fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "steam_roulette_db")
                .fallbackToDestructiveMigrationFrom(1)
                .addMigrations(Migration2to3())
                .build()
    }
}

private class Migration2to3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE owned_game ADD COLUMN shown INTEGER DEFAULT 0 NOT NULL");
    }
}