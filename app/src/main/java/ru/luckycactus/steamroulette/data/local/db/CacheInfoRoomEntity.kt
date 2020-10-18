package ru.luckycactus.steamroulette.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_info")
data class CacheInfoRoomEntity(
    @PrimaryKey val key: String,
    val timestamp: Long
)