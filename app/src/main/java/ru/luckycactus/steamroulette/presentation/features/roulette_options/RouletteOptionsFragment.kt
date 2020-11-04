package ru.luckycactus.steamroulette.presentation.features.roulette_options

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_roulette_options.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.observe
import ru.luckycactus.steamroulette.presentation.utils.extensions.showIfNotExist

@AndroidEntryPoint
class RouletteOptionsFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {
    private val viewModel: RouletteOptionsViewModel by viewModels()

    override val layoutResId = R.layout.fragment_roulette_options

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prefViewMaxPlaytime.setOnClickListener {
            dismiss()
            parentFragmentManager.showIfNotExist(PLAYTIME_DIALOG_TAG) {
                RouletteFiltersDialog.newInstance()
            }
        }

        prefViewHiddenGames.setOnClickListener {
            viewModel.onHiddenGamesClick()
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.playTimePrefValue) {
            prefViewMaxPlaytime.value = it
        }

        observe(viewModel.hiddenGamesCount) {
            prefViewHiddenGames.value = it.toString()
            val enabled = it > 0
            prefViewHiddenGames.isEnabled = enabled
        }
    }

    companion object {
        fun newInstance() = RouletteOptionsFragment()
        private const val PLAYTIME_DIALOG_TAG = "playtime_dialog_tag"
    }
}