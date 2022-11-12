package ru.luckycactus.steamroulette.presentation.features.game_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument

@AndroidEntryPoint
class GameDetailsFragment : BaseFragment() {

    private val viewModel: GameDetailsViewModel by viewModels()

    private var initialTintColor: Int by argument()
    private var game: GameHeader by argument(GameDetailsViewModel.ARG_GAME)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    GameDetailsRoute(
                        viewModel = viewModel,
                        Color(initialTintColor),
                        onBackPressed = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(game: GameHeader, color: Int) =
            GameDetailsFragment().apply {
                this.initialTintColor = color
                this.game = game
            }
    }
}