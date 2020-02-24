package ru.luckycactus.steamroulette.data.repositories.login.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResolveVanityUrlResponse(
    @Json(name="response") val result: ResolveVanityUrlResult
)

@JsonClass(generateAdapter = true)
data class ResolveVanityUrlResult(
    @Json(name="steamid") val steamId: String?,
    @Json(name="success") val success: Int = 0
)