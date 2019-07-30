package ru.luckycactus.steamroulette.data.games.mapper

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.data.model.OwnedGameRoomEntity
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.entity.SteamId

class OwnedGameRoomEntityMapper private constructor(
    private val steam64: Long
): Mapper<OwnedGameEntity, OwnedGameRoomEntity>() {

    override fun mapFrom(from: OwnedGameEntity): OwnedGameRoomEntity {
        return OwnedGameRoomEntity(
            steam64,
            false, //todo!!!
            from
        )
    }

    class Factory {
        fun create(steam64: Long) = OwnedGameRoomEntityMapper(steam64)
    }
}