package ru.luckycactus.steamroulette.data.repositories.games.owned.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//data class OwnedGamesResponse(
//    @Json(name="response") val ownedGameResult: OwnedGamesResult?
//)
//
//data class OwnedGamesResult(
//    @Json(name="game_count") val gameCount: Int,
//    @Json(name="games") val gamesJson: List<OwnedGameEntity>
//)

@JsonClass(generateAdapter = true)
data class OwnedGameEntity(
    @Json(name="appid") val appId: Int,
    @Json(name="name") val name: String?,
    @Json(name="playtime_2weeks") val playtime2Weeks: Int = 0,
    @Json(name="playtime_forever") val playtimeForever: Int = 0,
    @Json(name="img_icon_url") val iconUrl: String? = null,
    @Json(name="img_logo_url") val logoUrl: String? = null,
    @Json(name="has_community_visible_stats") val hasCommunityVisibleStats: Boolean? = null
)