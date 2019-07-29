package ru.luckycactus.steamroulette.data.games.mapper

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity
import ru.luckycactus.steamroulette.domain.entity.OwnedGame
import ru.luckycactus.steamroulette.domain.common.Mapper

class OwnedGameMapper: Mapper<OwnedGameEntity, OwnedGame>() {

    override fun mapFrom(from: OwnedGameEntity): OwnedGame =
        OwnedGame(
            from.appId,
            from.name!!,
            from.playtime2Weeks,
            from.playtimeForever,
            from.iconUrl!!,
            from.logoUrl!!
        )

}
