package ru.luckycactus.steamroulette.data.net.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.luckycactus.steamroulette.data.repositories.games.details.models.GameStoreInfoResult

interface SteamStoreApiService {
    @GET("appdetails")
    suspend fun getGamesStoreInfo(
        @Query("appids") appIds: List<Int>,
        @Query("l") lang: String
    ): Map<Int, GameStoreInfoResult>
}