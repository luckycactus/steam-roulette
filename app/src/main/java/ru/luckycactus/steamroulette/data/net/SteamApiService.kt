package ru.luckycactus.steamroulette.data.net

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import ru.luckycactus.steamroulette.data.repositories.login.models.ResolveVanityUrlResponse
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummariesResponse

interface SteamApiService {

    @Streaming
    @GET("IPlayerService/GetOwnedGames/v0001")
    suspend fun getOwnedGames(
        @Query("steamid") steamId: Long,
        @Query("include_appinfo") includeAppInfo: Boolean?,
        @Query("include_played_free_games") includePlayedFreeGames: Boolean?
    ): ResponseBody

    @GET("ISteamUser/ResolveVanityURL/v0001")
    suspend fun resolveVanityUrl(
        @Query("vanityurl") vanityUrl: String
    ): ResolveVanityUrlResponse

    @GET("ISteamUser/GetPlayerSummaries/v0002")
    suspend fun getUserSummaries(
        @Query("steamids") steamIds: List<Long>
    ): UserSummariesResponse
}