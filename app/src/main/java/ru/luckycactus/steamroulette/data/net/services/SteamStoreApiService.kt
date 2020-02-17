package ru.luckycactus.steamroulette.data.net.services

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamStoreApiService {
    @GET("appdetails")
    suspend fun getGamesStoreInfo(
        @Query("appids") appIds: List<Int>,
        @Query("l") lang: String
    ): ResponseBody
}