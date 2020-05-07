package ru.luckycactus.steamroulette.data.local.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity

@Dao
abstract class UserSummaryDao : BaseDao<UserSummaryEntity>() {

    @Query("select * from user_summary where steam64 = :steam64")
    abstract suspend fun get(steam64: Long): UserSummaryEntity

    @Query("select * from user_summary where steam64 = :steam64")
    abstract fun observe(steam64: Long): Flow<UserSummaryEntity?>

    @Query("delete from user_summary where steam64 = :steam64")
    abstract suspend fun delete(steam64: Long)
}