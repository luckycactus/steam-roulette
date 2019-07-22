package ru.luckycactus.steamroulette.data.net

import retrofit2.http.GET
import retrofit2.http.Query
import ru.luckycactus.steamroulette.data.model.OwnedGamesResponse
import ru.luckycactus.steamroulette.data.model.ResolveVanityUrlResponse
import ru.luckycactus.steamroulette.data.model.UserSummariesResponse

interface SteamApiService {

    @GET("IPlayerService/GetOwnedGames/v0001")
    suspend fun getOwnedGames(
        @Query("steamid") steamId: Long,
        @Query("include_appinfo") includeAppInfo: Boolean?,
        @Query("include_played_free_games") includePlayedFreeGames: Boolean?
    ): OwnedGamesResponse

    @GET("ISteamUser/ResolveVanityURL/v0001")
    suspend fun resolveVanityUrl(
        @Query("vanityurl") vanityUrl: String
    ): ResolveVanityUrlResponse

    @GET("/ISteamUser/GetPlayerSummaries/v0002")
    suspend fun getUserSummaries(
        @Query("steamids") steamIds: List<Long>
    ): UserSummariesResponse
}