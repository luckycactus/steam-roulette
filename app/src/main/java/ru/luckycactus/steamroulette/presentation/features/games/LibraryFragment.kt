package ru.luckycactus.steamroulette.presentation.features.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme

@AndroidEntryPoint
class LibraryFragment : BaseFragment() {

    private val viewModel: LibraryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    LibraryRoute(
                        viewModel = viewModel,
                        onBackClick = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance() = LibraryFragment()
    }
}