package ru.luckycactus.steamroulette.data.model

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "owned_game", primaryKeys = ["userSteam64", "appId"])
data class OwnedGameRoomEntity(
    val userSteam64: Long,

    val hidden: Boolean,

    val updateTimeStamp: Long,

    @Embedded
    val ownedGameEntity: OwnedGameEntity
)