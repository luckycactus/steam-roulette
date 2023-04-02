package ru.luckycactus.steamroulette.presentation.ui.compose

import ru.luckycactus.steamroulette.domain.games.entity.*
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

    val systemReqs = listOf(
        SystemRequirements(
            Platform.Windows,
            minimum = "<strong>Minimum:</strong><br><ul class=\"bb_ul\"><li><strong>OS:</strong>Windows XP SP3 (32 bit) / Vista<br>\t</li><li><strong>Processor:</strong>Intel(R) Core(TM)2 Duo 2.4, AMD Athlon(TM) X2 2.8 Ghz<br>\t</li><li><strong>Memory:</strong>2 GB RAM<br>\t</li><li><strong>Graphics:</strong>Geforce 9600 GS, Radeon HD4000, Shader Model 3.0, 512 MB<br>\t</li><li><strong>DirectX®:</strong>9.0c<br>\t</li><li><strong>Hard Drive:</strong>3 GB HD space<br>\t</li><li><strong>Sound:</strong>DirectX compatible</li></ul>",
            recommended = "<strong>Recommended:</strong><br><ul class=\"bb_ul\"><li><strong>OS:</strong>Windows 7<br>\t</li><li><strong>Processor:</strong>Intel(R) Core(TM)2 Quad 2.7 Ghz, AMD Phenom(TM)II X4 3 Ghz<br>\t</li><li><strong>Memory:</strong>4 GB RAM<br>\t</li><li><strong>Graphics:</strong>GeForce GTX 260, Radeon HD 5770, 1024 MB, Shader Model 3.0<br>\t</li><li><strong>DirectX®:</strong>9.0c<br>\t</li><li><strong>Hard Drive:</strong>3 GB HD space<br>\t</li><li><strong>Sound:</strong>DirectX compatible<br>\t</li><li><strong>Other Requirements:</strong>Broadband Internet connection</li></ul>"
        ),
        SystemRequirements(
            Platform.Mac,
            minimum = "<strong>Minimum:</strong><br><ul class=\"bb_ul\"><li><strong>OS:</strong>10.6<br>\t</li><li><strong>Processor:</strong>Intel(R) Core(TM)2 Duo 2.4, AMD Athlon(TM) X2 2.8 Ghz<br>\t</li><li><strong>Memory:</strong>2 GB RAM<br>\t</li><li><strong>Graphics:</strong>Geforce 9600 GS, Radeon HD4000, Shader Model 3.0, 512 MB<br>\t</li><li><strong>Hard Drive:</strong>3 GB HD space<br>\t</li><li><strong>Sound:</strong>Integrated</li></ul>",
            recommended = "<strong>Recommended:</strong><br><ul class=\"bb_ul\"><li><strong>OS:</strong>10.6<br>\t</li><li><strong>Processor:</strong>Intel(R) Core(TM)2 Quad 2.7 Ghz, AMD Phenom(TM)II X4 3 Ghz<br>\t</li><li><strong>Memory:</strong>4 GB RAM<br>\t</li><li><strong>Graphics:</strong>GeForce GTX 260, Radeon HD 5770, 1024 MB, Shader Model 3.0<br>\t</li><li><strong>Hard Drive:</strong>3 GB HD space<br>\t</li><li><strong>Sound:</strong>Integrated<br>\t</li><li><strong>Other Requirements:</strong>Broadband Internet connection</li></ul>"
        ),
        SystemRequirements(
            Platform.Linux,
            minimum = "<strong>Minimum:</strong><br><ul class=\"bb_ul\"><li><strong>OS:</strong>Ubuntu 12.04<br>\t</li><li><strong>Processor:</strong>Intel(R) Core(TM)2 Duo 2.4, AMD Athlon(TM) X2 2.8 Ghz<br>\t</li><li><strong>Memory:</strong>2 GB RAM<br>\t</li><li><strong>Graphics:</strong>Geforce 9600 GS, Radeon HD4000, Shader Model 3.0, 512 MB<br>\t</li><li><strong>Hard Drive:</strong>3 GB HD space</li></ul>",
            recommended = "<strong>Recommended:</strong><br><ul class=\"bb_ul\"><li><strong>OS:</strong>Ubuntu 12.04<br>\t</li><li><strong>Processor:</strong>Intel(R) Core(TM)2 Quad 2.7 Ghz, AMD Phenom(TM)II X4 3 Ghz<br>\t</li><li><strong>Memory:</strong>4 GB RAM<br>\t</li><li><strong>Graphics:</strong>GeForce GTX 260, Radeon HD 5770, 1024 MB, Shader Model 3.0<br>\t</li><li><strong>Hard Drive:</strong>3 GB HD space<br>\t</li><li><strong>Other Requirements:</strong>Broadband Internet connection</li></ul>"
        )
    )

    val games = listOf(
        GameHeader(39140, "FINAL FANTASY VII"),
        GameHeader(2050650, "Resident Evil 4"),
        GameHeader(1328670, "Mass Effect™ Legendary Edition"),
        GameHeader(990080, "Hogwarts Legacy"),

    )
}