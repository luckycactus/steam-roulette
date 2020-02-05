package ru.luckycactus.steamroulette.data.repositories.games.models

import com.google.gson.annotations.SerializedName
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo

data class GameStoreInfoResult(
    @SerializedName("data") val gameStoreInfo: GameStoreInfo?,
    @SerializedName("success") val success: Boolean
)