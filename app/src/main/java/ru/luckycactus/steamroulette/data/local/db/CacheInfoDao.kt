package ru.luckycactus.steamroulette.data.local.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class CacheInfoDao : BaseDao<CacheInfoRoomEntity>() {

    suspend fun getTimestamp(key: String) = _getTimestamp(key) ?: 0L

    @Query("SELECT timestamp FROM cache_info WHERE `key` = :key")
    abstract suspend fun _getTimestamp(key: String): Long?

    @Query("DELETE FROM cache_info WHERE `key` = :key")
    abstract suspend fun delete(key: String)

    @Query("DELETE FROM cache_info")
    abstract suspend fun clear()

    fun observeTimestamp(key: String): Flow<Long> =
        _observeTimestamp(key).map { it ?: 0 }

    @Query("SELECT timestamp FROM cache_info WHERE `key` = :key")
    abstract fun _observeTimestamp(key: String): Flow<Long?>
}