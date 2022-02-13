package ru.luckycactus.steamroulette.presentation.features.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.presentation.features.main.MainActivity
import ru.luckycactus.steamroulette.presentation.ui.base.BaseFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme

@AndroidEntryPoint
class AboutFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    ProvideWindowInsets {
                        AboutScreen(
                            viewModel = viewModel(),
                            onBackClick = { requireActivity().onBackPressed() },
                            onRateClick = {
                                analytics.logClick("Review app")
                                (activity as MainActivity).reviewApp()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = AboutFragment()
    }
}