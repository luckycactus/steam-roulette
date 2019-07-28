package ru.luckycactus.steamroulette.data.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

data class OwnedGamesResponse(
    @SerializedName("response") val ownedGameEntity: OwnedGamesEntity
)

data class OwnedGamesEntity(
    @SerializedName("game_count") val gameCount: Int,
    @SerializedName("games") val games: List<OwnedGameEntity>
)

//@Entity(tableName = "owned_game")
data class OwnedGameEntity(
    @SerializedName("appid") val appId: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("playtime_2weeks") val playtime2Weeks: Int,
    @SerializedName("playtime_forever") val playtimeForever: Int,
    @SerializedName("img_icon_url") val iconUrl: String?,
    @SerializedName("img_logo_url") val logoUrl: String?,
    @SerializedName("has_community_visible_stats") val hasCommunityVisibleStats: Boolean?
)