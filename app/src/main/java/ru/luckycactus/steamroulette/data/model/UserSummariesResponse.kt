package ru.luckycactus.steamroulette.data.model

import com.google.gson.annotations.SerializedName

data class UserSummariesResponse(
    @SerializedName("response") val result: UserSummaryResult
)

data class UserSummaryResult(
    @SerializedName("players") val players: List<UserSummaryEntity>
)

data class UserSummaryEntity(
    @SerializedName("steamid") val steam64: String,
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