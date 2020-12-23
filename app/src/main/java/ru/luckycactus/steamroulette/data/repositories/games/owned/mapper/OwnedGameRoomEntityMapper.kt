package ru.luckycactus.steamroulette.data.repositories.games.owned.mapper

import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameMetaData
import ru.luckycactus.steamroulette.data.repositories.games.owned.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.core.Mapper

class OwnedGameRoomEntityMapper(
    private val steam64: Long,
    private val gamesMetaData: Map<Int, OwnedGameMetaData>
) : Mapper<OwnedGameEntity, OwnedGameRoomEntity>() {

    override fun mapFrom(from: OwnedGameEntity): OwnedGameRoomEntity {
        val metaData = gamesMetaData[from.appId]
        return OwnedGameRoomEntity(
            steam64,
            metaData?.hidden ?: false,
            metaData?.shown ?: false,
            from
        )
    }
}