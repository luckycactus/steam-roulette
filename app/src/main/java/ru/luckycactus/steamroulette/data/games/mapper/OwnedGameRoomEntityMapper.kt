package ru.luckycactus.steamroulette.data.games.mapper

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.entity.SteamId

class OwnedGameRoomEntityMapper(
    private val steam64: Long,
    private val hiddenGameIds: Set<Long>,
    private val timestamp: Long
) : Mapper<OwnedGameEntity, OwnedGameRoomEntity>() {

    override fun mapFrom(from: OwnedGameEntity): OwnedGameRoomEntity {
        return OwnedGameRoomEntity(
            steam64,
            hiddenGameIds.contains(from.appId),
            timestamp,
            from
        )

    }
}