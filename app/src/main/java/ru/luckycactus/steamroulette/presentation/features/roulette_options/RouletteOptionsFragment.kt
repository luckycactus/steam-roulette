package ru.luckycactus.steamroulette.presentation.features.roulette_options

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.luckycactus.steamroulette.databinding.FragmentRouletteOptionsBinding
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.observe
import ru.luckycactus.steamroulette.presentation.utils.extensions.showIfNotExist

@AndroidEntryPoint
class RouletteOptionsFragment : BaseBottomSheetDialogFragment(),
    MessageDialogFragment.Callbacks {

    private val binding by viewBinding(FragmentRouletteOptionsBinding::bind)

    private val viewModel: RouletteOptionsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

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