package ru.luckycactus.steamroulette.presentation.features.game_details.model

import dagger.Reusable
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils
import javax.inject.Inject

@Reusable
class GameDetailsUiModelMapper @Inject constructor(
    private val resourceManager: ResourceManager
) : Mapper<GameStoreInfo, List<GameDetailsUiModel>>() {

    override fun mapFrom(from: GameStoreInfo): List<GameDetailsUiModel> =
        mutableListOf<GameDetailsUiModel>().apply {
            add(mapHeader(from))
            add(GameDetailsUiModel.Links)
            add(mapShortDescription(from))
        }

    private fun mapHeader(from: GameStoreInfo): GameDetailsUiModel.Header {
        val releaseDate = from.releaseDate?.let {
            if (!it.date.isNullOrBlank()) {
                it.date
            } else if (it.comingSoon) {
                resourceManager.getString(R.string.coming_soon)
            } else null
        }

        return GameDetailsUiModel.Header(
            GameMinimal(from),
            from.developers?.joinToString(),
            from.publishers?.joinToString(),
            releaseDate
        )
    }

    private fun mapShortDescription(from: GameStoreInfo): GameDetailsUiModel.ShortDescription =
        GameDetailsUiModel.ShortDescription(
            from.shortDescription,//.replace("&quot", "\""), //todo
            from.categories.map { it.description },
            from.genres.map { it.description }
        )
}