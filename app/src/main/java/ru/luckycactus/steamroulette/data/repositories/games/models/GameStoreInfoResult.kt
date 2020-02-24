package ru.luckycactus.steamroulette.data.repositories.games.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo

@JsonClass(generateAdapter = true)
data class GameStoreInfoResult(
    @Json(name="data") val gameStoreInfo: GameStoreInfo?,
    @Json(name="success") val success: Boolean = false
)