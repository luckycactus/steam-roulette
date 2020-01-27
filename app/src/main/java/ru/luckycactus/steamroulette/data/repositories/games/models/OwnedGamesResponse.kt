package ru.luckycactus.steamroulette.data.repositories.games.models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class OwnedGamesResponse(
    @SerializedName("response") val ownedGameResult: OwnedGamesResult?
)

data class OwnedGamesResult(
    @SerializedName("game_count") val gameCount: Int,
    @SerializedName("games") val gamesJson: JsonElement
)

data class OwnedGameEntity(
    @SerializedName("appid") val appId: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("playtime_2weeks") val playtime2Weeks: Int,
    @SerializedName("playtime_forever") val playtimeForever: Int,
    @SerializedName("img_icon_url") val iconUrl: String?,
    @SerializedName("img_logo_url") val logoUrl: String?,
    @SerializedName("has_community_visible_stats") val hasCommunityVisibleStats: Boolean?
)