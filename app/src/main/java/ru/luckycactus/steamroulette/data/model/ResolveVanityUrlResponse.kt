package ru.luckycactus.steamroulette.data.model

import com.google.gson.annotations.SerializedName

data class ResolveVanityUrlResponse(
    @SerializedName("response") val result: ResolveVanityUrlResult
)

data class ResolveVanityUrlResult(
    @SerializedName("steamid") val steamId: String?,
    @SerializedName("success") val success: Int
)