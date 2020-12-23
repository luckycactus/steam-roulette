package ru.luckycactus.steamroulette.util

import okhttp3.MediaType.Companion.toMediaType
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.SteamId

object TestData {

    val jsonMediaType = "application/json".toMediaType()
    val testSteamId = SteamId.fromSteam64(76561197960287930L)
    val userSummaryEntityDummy = UserSummaryEntity(
        testSteamId.as64(),
        1,
        0,
        "",
        0,
        "",
        "",
        "",
        "",
        0
    )
    val ownedGameEntityDummy = OwnedGameEntity(
        1, "", 0, 0, "", "", true
    )

    /**
     * parsed data of json/games/owned_games_response_success.json
     */
    val ownedGamesData = listOf(
        OwnedGameEntity(
            17460,
            "Mass Effect",
            0,
            1751,
            "57be81f70afa48c65437df93d75ba167a29687bc",
            "7501ea5009533fa5c017ec1f4b94725d67ad4936",
            true
        ),
        OwnedGameEntity(
            30,
            "Day of Defeat",
            0,
            0,
            "aadc0ce51ff6ba2042d633f8ec033b0de62091d0",
            "beff21c4d29d2579e794c930bae599cd0c8a8f17",
            true
        ),
        OwnedGameEntity(
            40,
            "Deathmatch Classic",
            20,
            30,
            "c525f76c8bc7353db4fd74b128c4ae2028426c2a",
            "4bb69695ef9d0ae73e73488fb6456aa4ea1215fa",
            true
        )
    )

    val ownedGamesDataUpdated = listOf(
        OwnedGameEntity(
            17460,
            "Mass Effect",
            50,
            1801,
            "57be81f70afa48c65437df93d75ba167a29687bc",
            "7501ea5009533fa5c017ec1f4b94725d67ad4936",
            true
        ),
        OwnedGameEntity(
            31,
            "Half-Life",
            400,
            400,
            "aadc0ce51ff6ba2042d633f8ec033b0de62091d0",
            "beff21c4d29d2579e794c930bae599cd0c8a8f17",
            false
        ),
        OwnedGameEntity(
            40,
            "Deathmatch Classic",
            20,
            30,
            "c525f76c8bc7353db4fd74b128c4ae2028426c2a",
            "",
            true
        )
    )
}