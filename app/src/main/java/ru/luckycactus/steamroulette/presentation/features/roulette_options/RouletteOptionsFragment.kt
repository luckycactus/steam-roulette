package ru.luckycactus.steamroulette.presentation.features.roulette_options

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_options_filter.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.findComponent
import ru.luckycactus.steamroulette.presentation.ui.base.BaseBottomSheetDialogFragment
import ru.luckycactus.steamroulette.presentation.features.main.MainFlowComponent
import ru.luckycactus.steamroulette.presentation.ui.widget.MessageDialogFragment
import ru.luckycactus.steamroulette.presentation.utils.observe
import ru.luckycactus.steamroulette.presentation.utils.showIfNotExist
import ru.luckycactus.steamroulette.presentation.utils.viewModel


class RouletteOptionsFragment : BaseBottomSheetDialogFragment(), MessageDialogFragment.Callbacks {

    private val viewModel by viewModel {
        findComponent<MainFlowComponent>().rouletteOptionsViewModel
    }

    override val layoutResId = R.layout.fragment_options_filter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prefViewMaxPlaytime.setOnClickListener {
            dismiss()
            parentFragmentManager.showIfNotExist(PLAYTIME_DIALOG_TAG) {
                PlaytimePrefDialog.newInstance()
            }
        }

        observe(viewModel.closeAction) {
            dismiss()
        }

        observe(viewModel.playTimePrefValue) {
            prefViewMaxPlaytime.value = it
        }

        observe(viewModel.hiddenGamesCount) {
            tvHiddenGamesCount.text = it.toString()
            val enabled = it > 0
            btnClearHiddenGames.isEnabled = enabled
            tvHiddenGamesCount.isEnabled = enabled
            tvHiddenGamesLabel.isEnabled = enabled
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