package ru.luckycactus.steamroulette.data.repositories.user.mapper

import ru.luckycactus.steamroulette.data.repositories.user.models.UserSummaryEntity
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.user.entity.CommunityVisibleState
import ru.luckycactus.steamroulette.domain.user.entity.PersonaState
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

class UserSummaryMapper @Inject constructor() : Mapper<UserSummaryEntity, UserSummary>() {

    override fun mapFrom(from: UserSummaryEntity): UserSummary =
        UserSummary(
            SteamId.fromSteam64(from.steam64),
            from.personaName,
            CommunityVisibleState.fromInt(
                from.communityVisibilityState
            ),
            from.profileState == 1,
            from.lastLogoff,
            from.profileUrl,
            from.avatar,
            from.avatarMedium,
            from.avatarFull,
            PersonaState.fromInt(
                from.personaState
            )
        )
}
