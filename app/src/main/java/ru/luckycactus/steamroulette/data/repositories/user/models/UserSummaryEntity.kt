package ru.luckycactus.steamroulette.data.repositories.user.models

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

data class UserSummariesResponse(
    @SerializedName("response") val result: UserSummaryResult
)

data class UserSummaryResult(
    @SerializedName("players") val players: List<UserSummaryEntity>
)

@Entity(tableName = "user_summary", primaryKeys = ["steam64"])
data class UserSummaryEntity(
    @SerializedName("steamid") val steam64: Long,
    @SerializedName("communityvisibilitystate") val communityVisibilityState: Int,
    @SerializedName("profilestate") val profileState: Int,
    @SerializedName("personaname") val personaName: String,
    @SerializedName("lastlogoff") val lastLogoff: Long,
    @SerializedName("profileurl") val profileUrl: String,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("avatarmedium") val avatarMedium: String,
    @SerializedName("avatarfull") val avatarFull: String,
    @SerializedName("personastate") val personaState: Int
)