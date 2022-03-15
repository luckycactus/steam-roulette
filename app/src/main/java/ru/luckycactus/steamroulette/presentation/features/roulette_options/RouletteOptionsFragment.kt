package ru.luckycactus.steamroulette.presentation.features.roulette_options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.observe
import ru.luckycactus.steamroulette.presentation.utils.extensions.showIfNotExist

@AndroidEntryPoint
class RouletteOptionsFragment : BaseBottomSheetDialogFragment(),
    MessageDialogFragment.Callbacks {

    private val viewModel: RouletteOptionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SteamRouletteTheme {
                    ProvideWindowInsets {
                        RouletteOptionsScreen(viewModel, ::onPlaytimeClick)
                    }
                }
            }
        }
    }

    private fun onPlaytimeClick() {
        dismiss()
        parentFragmentManager.showIfNotExist(PLAYTIME_DIALOG_TAG) {
            RouletteFiltersDialog.newInstance()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.closeAction) {
            dismiss()
        }
    }

    companion object {
        fun newInstance() = RouletteOptionsFragment()

        private const val PLAYTIME_DIALOG_TAG = "playtime_dialog_tag"
    }
}