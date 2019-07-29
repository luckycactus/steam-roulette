package ru.luckycactus.steamroulette.data.user.mapper

import ru.luckycactus.steamroulette.data.model.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.entity.CommunityVisibleState
import ru.luckycactus.steamroulette.domain.entity.PersonaState
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

class UserSummaryMapper : Mapper<UserSummaryEntity, UserSummary>() {

    override fun mapFrom(from: UserSummaryEntity): UserSummary =
        UserSummary(
            SteamId.parse(
                from.steam64,
                SteamId.Format.Steam64
            ),
            from.personaName,
            CommunityVisibleState.fromInt(from.communityVisibilityState),
            from.profileState == 1,
            from.lastLogoff,
            from.profileUrl,
            from.avatar,
            from.avatarMedium,
            from.avatarFull,
            PersonaState.fromInt(from.personaState)
        )
}
