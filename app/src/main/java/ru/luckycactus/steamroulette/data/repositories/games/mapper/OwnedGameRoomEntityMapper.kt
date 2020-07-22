package ru.luckycactus.steamroulette.data.repositories.games.mapper

import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameAppData
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.core.Mapper

class OwnedGameRoomEntityMapper(
    private val steam64: Long,
    private val gamesAppData: Map<Int, OwnedGameAppData>
) : Mapper<OwnedGameEntity, OwnedGameRoomEntity>() {

    override fun mapFrom(from: OwnedGameEntity): OwnedGameRoomEntity {
        val appData = gamesAppData[from.appId]
        return OwnedGameRoomEntity(
            steam64,
            appData?.hidden ?: false,
            appData?.shown ?: false,
            from
        )
    }
}