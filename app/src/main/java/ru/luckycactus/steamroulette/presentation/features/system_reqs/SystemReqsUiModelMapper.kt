package ru.luckycactus.steamroulette.presentation.features.system_reqs

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.Mapper
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.PlatformsAvailability
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.features.system_reqs.model.SystemReqsUiModel
import javax.inject.Inject

@Reusable
class SystemReqsUiModelMapper @Inject constructor(
) : Mapper<GameStoreInfo, List<SystemReqsUiModel>>() {

    private val extraSpacesRegex = "<br>\\s*</li>".toRegex()

    override fun mapFrom(from: GameStoreInfo): List<SystemReqsUiModel> {
        return listOfNotNull(
            //todo
            mapPlatformReqs("PC", from.pcRequirements, from.platforms?.windows),
            mapPlatformReqs("Mac", from.macRequirements, from.platforms?.mac),
            mapPlatformReqs("Linux", from.linuxRequirements, from.platforms?.linux)
        )
    }

    private fun mapPlatformReqs(
        platform: String,
        systemRequirements: SystemRequirements?,
        platformAvailable: Boolean?
    ): SystemReqsUiModel? {
        return if (
            systemRequirements != null &&
            (systemRequirements.minimum != null || systemRequirements.recommended != null) &&
            platformAvailable != false
        ) {
            SystemReqsUiModel(
                platform,
                systemRequirements.minimum?.replace(extraSpacesRegex, "<br></li>"),
                systemRequirements.recommended?.replace(extraSpacesRegex, "<br></li>")
            )
        } else null
    }
}