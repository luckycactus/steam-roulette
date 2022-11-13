package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.tabs.TabLayoutMediator
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.utils.extensions.addSystemTopPadding
import ru.luckycactus.steamroulette.presentation.utils.extensions.argument

class SystemReqsFragment : BaseFragment() {

    private var gameTitle: String by argument()
    private var systemReqs: List<SystemRequirements> by argument()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    SystemReqsRoute(
                        gameTitle = gameTitle,
                        systemReqs = systemReqs,
                        onBackClick = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(gameTitle: String, systemReqs: List<SystemRequirements>) =
            SystemReqsFragment().apply {
                this.gameTitle = gameTitle
                this.systemReqs = systemReqs
            }
    }
}