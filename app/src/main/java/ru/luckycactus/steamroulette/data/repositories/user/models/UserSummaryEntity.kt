package ru.luckycactus.steamroulette.data.repositories.user.models

import androidx.room.Entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserSummariesResponse(
    @Json(name="response") val result: UserSummaryResult
)

@JsonClass(generateAdapter = true)
data class UserSummaryResult(
    @Json(name="players") val players: List<UserSummaryEntity>
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "user_summary", primaryKeys = ["steam64"])
data class UserSummaryEntity(
    @Json(name="steamid") val steam64: Long,
    @Json(name="communityvisibilitystate") val communityVisibilityState: Int,
    @Json(name="profilestate") val profileState: Int = 0,
    @Json(name="personaname") val personaName: String,
    @Json(name="lastlogoff") val lastLogoff: Long = 0,
    @Json(name="profileurl") val profileUrl: String,
    @Json(name="avatar") val avatar: String,
    @Json(name="avatarmedium") val avatarMedium: String,
    @Json(name="avatarfull") val avatarFull: String,
    @Json(name="personastate") val personaState: Int = 0
)