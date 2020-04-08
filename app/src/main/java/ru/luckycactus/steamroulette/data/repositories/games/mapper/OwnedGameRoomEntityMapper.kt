package ru.luckycactus.steamroulette.data.repositories.games.mapper

import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameEntity
import ru.luckycactus.steamroulette.data.repositories.games.models.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.core.Mapper

class OwnedGameRoomEntityMapper(
    private val steam64: Long,
    private val hiddenGameIds: Set<Int>
) : Mapper<OwnedGameEntity, OwnedGameRoomEntity>() {

    override fun mapFrom(from: OwnedGameEntity): OwnedGameRoomEntity {
        return OwnedGameRoomEntity(
            steam64,
            hiddenGameIds.contains(from.appId),
            from
        )
    }
}