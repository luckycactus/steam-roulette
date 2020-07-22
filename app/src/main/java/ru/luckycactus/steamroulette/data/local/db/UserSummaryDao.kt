package ru.luckycactus.steamroulette.data.local.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity

@Dao
abstract class UserSummaryDao : BaseDao<UserSummaryEntity>() {

    @Query("SELECT * FROM user_summary WHERE steam64 = :steam64")
    abstract suspend fun get(steam64: Long): UserSummaryEntity

    @Query("SELECT * FROM user_summary WHERE steam64 = :steam64")
    abstract fun observe(steam64: Long): Flow<UserSummaryEntity?>

    @Query("DELETE FROM user_summary WHERE steam64 = :steam64")
    abstract suspend fun delete(steam64: Long)

    @Query("DELETE FROM user_summary")
    abstract suspend fun clear()
}