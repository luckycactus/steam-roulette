package ru.luckycactus.steamroulette.presentation.features.roulette_options

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_options_filter.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.showIfNotExist

@AndroidEntryPoint
class RouletteOptionsFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {
    private val viewModel: RouletteOptionsViewModel by viewModels()

    override val layoutResId = R.layout.fragment_options_filter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prefViewMaxPlaytime.setOnClickListener {
            dismiss()
            parentFragmentManager.showIfNotExist(PLAYTIME_DIALOG_TAG) {
                PlaytimePrefDialog.newInstance()
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
            btnClearHiddenGames.isEnabled = enabled
            prefViewHiddenGames.isEnabled = enabled
        }

        btnClearHiddenGames.setOnClickListener {
            MessageDialogFragment.create(
                context!!,
                messageResId = R.string.dialog_message_reset_hidden_games,
                negativeResId = R.string.cancel
            ).show(childFragmentManager, null)
        }
    }

    override fun onDialogPositiveClick(dialog: MessageDialogFragment, tag: String?) {
        viewModel.onClearHiddenGames()
    }

    companion object {
        fun newInstance() = RouletteOptionsFragment()
        private const val PLAYTIME_DIALOG_TAG = "playtime_dialog_tag"
    }
}