package ru.luckycactus.steamroulette.presentation.features.game_details.model

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.Mapper
import ru.luckycactus.steamroulette.domain.games.entity.GameMinimal
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GameDetailsUiModelMapper @Inject constructor(
) : Mapper<GameStoreInfo, List<GameDetailsUiModel>>() {

    override fun mapFrom(from: GameStoreInfo): List<GameDetailsUiModel> =
        mutableListOf<GameDetailsUiModel>().apply {
            add(GameDetailsUiModel.Header(GameMinimal(from)))
            add(GameDetailsUiModel.ShortDescription(from.detailedDescription))
        }
}