package ru.luckycactus.steamroulette.presentation.features.detailed_description

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument

class DetailedDescriptionFragment : BaseFragment() {

    private var gameTitle: String by argument()
    private var detailedDescription: String by argument()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    DetailedDescriptionRoute(
                        gameTitle = gameTitle,
                        description = detailedDescription,
                        onBackClick = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(gameTitle: String, detailedDescription: String) =
            DetailedDescriptionFragment().apply {
                this.gameTitle = gameTitle
                this.detailedDescription = detailedDescription
            }
    }
}