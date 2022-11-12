package ru.luckycactus.steamroulette.presentation.ui.compose

import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.MetacriticInfo
import ru.luckycactus.steamroulette.domain.games.entity.PlatformsAvailability
import ru.luckycactus.steamroulette.domain.games.entity.Screenshot
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsViewModel
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState

object PreviewData {
    val gameDetailsLoadingState = GameDetailsViewModel.UiState(
        GameDetailsUiModel(GameHeader(0, "FINAL FANTASY IX")),
        ContentState.Loading
    )
    val gameDetailsState = GameDetailsViewModel.UiState(
        GameDetailsUiModel(
            GameHeader(0, "FINAL FANTASY IX"),
            developer = "Squaresoft",
            publisher = "Square Enix",
            releaseDate = "2016",
            shortDescription = GameDetailsUiModel.ShortDescription(
                "Selling over five million copies since its release in 2000, FINAL FANTASY IX proudly returns on Steam! Now you can relive the adventures of Zidane and his crew on PC !",
                listOf("Single-player", "Masterpiece", "Single-player1", "Masterpiece1", "Single-player2", "Masterpiece2"),
                listOf("Action", "FPS", "Parkour", "Action1", "FPS1", "Parkour1", "Action2", "FPS2", "Parkour2"),
                18,
                MetacriticInfo(88, ""),
                true
            ),
            platforms = GameDetailsUiModel.Platforms(
                PlatformsAvailability(windows = true, mac = true, linux = true),
                systemRequirementsAvailable = true
            ),
            languages = "English",
            screenshots = listOf(
                Screenshot(0, "", ""),
                Screenshot(1, "", ""),
                Screenshot(2, "", "")
            )
        )
    )
}