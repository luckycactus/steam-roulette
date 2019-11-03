package ru.luckycactus.steamroulette.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "owned_game",
    primaryKeys = ["userSteam64", "appId"],
    foreignKeys = [ForeignKey(
        entity = UserSummaryEntity::class,
        parentColumns = ["steam64"],
        childColumns = ["userSteam64"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class OwnedGameRoomEntity(
    val userSteam64: Long,

    val hidden: Boolean,

    @Embedded
    val ownedGameEntity: OwnedGameEntity
)