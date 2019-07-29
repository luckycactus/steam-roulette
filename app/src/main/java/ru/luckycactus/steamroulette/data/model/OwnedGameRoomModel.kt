package ru.luckycactus.steamroulette.data.model

import androidx.room.Entity

@Entity(tableName = "owned_game", primaryKeys = ["userSteam64", "appId"])
data class OwnedGameRoomModel(
    val userSteam64: Long,
    val appId: Long,
    val name: String,
    val playtime2Weeks: Int,
    val playtimeForever: Int,
    val iconUrl: String,
    val logoUrl: String,
    val hasCommunityVisibleStats: Boolean
)