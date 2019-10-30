package ru.luckycactus.steamroulette.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.luckycactus.steamroulette.data.model.UserSummaryEntity

@Dao
abstract class UserSummaryDao {

    @Query("select * from user_summary where steam64 = :steam64")
    abstract suspend fun getUserSummary(steam64: Long): UserSummaryEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveUserSummaryToCache(userSummary: UserSummaryEntity)

    @Query("select * from user_summary where steam64 = :steam64")
    abstract fun observeUserSummary(steam64: Long): LiveData<UserSummaryEntity>

    @Query("delete from user_summary where steam64 = :steam64")
    abstract suspend fun removeUser(steam64: Long)
}